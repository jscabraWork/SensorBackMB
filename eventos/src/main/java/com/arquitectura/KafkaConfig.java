package com.arquitectura;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.error.RetryableException;

import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

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

	// Consumer configuration
	@Value("${spring.kafka.consumer.bootstrap-servers}")
	private String consumerBootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroupId;

	@Value("${spring.kafka.consumer.auto-offset-reset}")
	private String consumerAutoOffsetReset;

	@Value("${spring.kafka.consumer.isolation-level}")
	private String consumerIsolationLevel;

	@Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
	private String consumerTrustedPackages;

	// Topics
	@Value("${eventos.topic}")
	private String eventosTopic;

	@Value("${dias.topic}")
	private String diasTopic;

	@Value("${tarifas.topic}")
	private String tarifasTopic;

	@Value("${temporadas.topic}")
	private String temporadasTopic;

	@Value("${localidades.topic}")
	private String localidadesTopic;

	@Value("${imagenes.topic}")
	private String imagenesTopic;

	@Value("${organizador.topic}")
	private String organizadorTopic;

	@Value("${ciudades.topic}")
	private String ciudadesTopic;

	@Value("${venues.topic}")
	private String venuesTopic;

	@Value("${coordinadores.topic}")
	private String coordinadoresTopic;

	@Value("${tipos-eventos.topic}")
	private String tiposTopic;

	@Autowired
	Environment environment;

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
		config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalIdPrefix);
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
		return new KafkaTransactionManager<>(producerFactory);
	}

	@Bean("transactionManager")
	JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	// Topic Beans - Todos los topics definidos
	@Bean
	NewTopic eventosTopic() {
		return TopicBuilder.name(eventosTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic tiposEventosTopic() {
		return TopicBuilder.name(tiposTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic diasTopic() {
		return TopicBuilder.name(diasTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic tarifasTopic() {
		return TopicBuilder.name(tarifasTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic temporadasTopic() {
		return TopicBuilder.name(temporadasTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic localidadesTopic() {
		return TopicBuilder.name(localidadesTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic imagenesTopic() {
		return TopicBuilder.name(imagenesTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic organizadorTopic() {
		return TopicBuilder.name(organizadorTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic ciudadesTopic() {
		return TopicBuilder.name(ciudadesTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}

	@Bean
	NewTopic venuesTopic() {
		return TopicBuilder.name(venuesTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}



	@Bean
	ConsumerFactory<String, Object> consumerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
		config.put(JsonDeserializer.TRUSTED_PACKAGES, consumerTrustedPackages);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
		config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, consumerIsolationLevel.toLowerCase());
		config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		config.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
		config.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required ;");
		config.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
				new FixedBackOff(5000, 3));
		errorHandler.addNotRetryableExceptions(NotRetryableException.class);
		errorHandler.addRetryableExceptions(RetryableException.class);

		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}
}