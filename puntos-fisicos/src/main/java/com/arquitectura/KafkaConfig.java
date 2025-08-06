package com.arquitectura;

import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

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

	// Topics
	@Value("${eventos-puntosfisicos.topic}")
	private String eventosPuntoFisicoTopic;

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
		return TopicBuilder.name(eventosPuntoFisicoTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas", "2"))
				.build();
	}
}