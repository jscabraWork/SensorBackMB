package com.arquitectura;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.error.RetryableException;
import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Autowired
	Environment environment;

	@Value("${spring.kafka.producer.key-serializer}")
	private String keySerializer;

	@Value("${spring.kafka.producer.value-serializer}")
	private String valueSerializer;

	@Value("${spring.kafka.producer.acks}")
	private String acks;

	@Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
	private String deliveryTimeout;

	@Value("${spring.kafka.producer.properties.linger.ms}")
	private String linger;

	@Value("${spring.kafka.producer.properties.request.timeout.ms}")
	private String requestTimeout;

	@Value("${spring.kafka.producer.properties.enable.idempotence}")
	private boolean idempotence;

	@Value("${spring.kafka.producer.properties.max.in.flight.request.per.connection}")
	private Integer inflightRequests;

	@Value("${spring.kafka.producer.transaction-id-prefix}")
	private String transactionalIdPrefix;

	@Value("${eventos-promotores.topic}")
	private String eventosPromotoresTopic;

	// =========================
	// PRODUCER CONFIGURATION
	// =========================

	Map<String, Object> producerConfigs() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
		config.put(ProducerConfig.ACKS_CONFIG, acks);
		config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
		config.put(ProducerConfig.LINGER_MS_CONFIG, linger);
		config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
		config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
		config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, inflightRequests);

		// CRITICAL FIX: Unique transactional ID to avoid conflicts
		String uniqueTransactionalId = transactionalIdPrefix + "-" + UUID.randomUUID().toString().substring(0, 8);
		config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, uniqueTransactionalId);
		logger.info("Producer configured with transactional ID: {}", uniqueTransactionalId);

		// AWS MSK Configuration
		config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		config.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
		config.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required ;");
		config.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
		return config;
	}

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		configs.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
		configs.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required ;");
		configs.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
		configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
		configs.put(AdminClientConfig.RETRIES_CONFIG, 5);
		configs.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
		configs.put(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000);
		return new KafkaAdmin(configs);
	}

	@Bean
	ProducerFactory<String, Object> producerFactory() {
		return new DefaultKafkaProducerFactory<String, Object>(producerConfigs());
	}

	@Bean
	KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<String, Object>(producerFactory());
	}

	@Bean("kafkaTransactionManager")
	KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
		KafkaTransactionManager<String, Object> tm = new KafkaTransactionManager<>(producerFactory);
		logger.info("Kafka transaction manager configured");
		return tm;
	}

	@Bean("transactionManager")
	JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager tm = new JpaTransactionManager(entityManagerFactory);
		logger.info("JPA transaction manager configured as primary");
		return tm;
	}

	// =========================
	// CONSUMER CONFIGURATION
	// =========================

	@Bean
	public ConsumerFactory<String, Object> consumerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.consumer.bootstrap-servers", bootstrapServers));
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
		config.put(JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
		config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
		config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, environment.getProperty("spring.kafka.consumer.isolation-level", "read_committed").toLowerCase());

		// Additional configuration for robustness
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
		config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);
		config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);

		// AWS MSK Configuration
		config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		config.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
		config.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required ;");
		config.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

		logger.info("Consumer configured with isolation level: {}", config.get(ConsumerConfig.ISOLATION_LEVEL_CONFIG));
		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {

		// Enhanced Dead Letter Publisher with detailed logging
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(consumerRecord, exception) -> {
					String originalTopic = consumerRecord.topic();
					String dltTopic = originalTopic + "-dlt";
					logger.error("Sending message to DLT: {} -> {} | Offset: {} | Reason: {}",
							originalTopic, dltTopic, consumerRecord.offset(), exception.getMessage());
					return new TopicPartition(dltTopic, consumerRecord.partition());
				}
		);

		// Exponential backoff: 1s, 2s, 4s (3 attempts)
		ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
		backOff.setInitialInterval(1000L);
		backOff.setMultiplier(2.0);
		backOff.setMaxInterval(8000L);

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

		// Exception classification
		errorHandler.addNotRetryableExceptions(
				NotRetryableException.class,
				IllegalArgumentException.class
		);

		errorHandler.addRetryableExceptions(
				RetryableException.class,
				org.springframework.dao.DataAccessException.class
		);

		// Detailed retry logging
		errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
			logger.warn("RETRY {}/3 - Topic: {} | Offset: {} | Partition: {} | Error: {}",
					deliveryAttempt, record.topic(), record.offset(), record.partition(), ex.getMessage());
		});

		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);

		// Container configuration for robustness
		factory.setConcurrency(1);
		factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
		factory.getContainerProperties().setSyncCommits(true);

		logger.info("Kafka listener container factory configured with error handler and retry policy");
		return factory;
	}

	// =========================
	// TOPIC CREATION
	// =========================

	@Bean
	NewTopic eventosTopic() {
		NewTopic topic = TopicBuilder.name(eventosPromotoresTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
		logger.info("Configured topic: {}", eventosPromotoresTopic);
		return topic;
	}
}