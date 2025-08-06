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
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.producer.key-serializer}")
	private String keySerializer;

	@Value("${spring.kafka.producer.value-serializer}")
	private String valueSerializer;

	@Value("${spring.kafka.producer.acks}")
	private String acks;

	@Value("${spring.kafka.producer.properties..delivery.timeout.ms}")
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

	@Value("${admins.topic}")
	private String adminsTopic;

	@Value("${clientes.topic}")
	private String clientesTopic;

	@Value("${organizador.topic}")
	private String organizadorTopic;

	@Value("${promotor.topic}")
	private String promotorTopic;

	@Value("${puntoF.topic}")
	private String puntoFTopic;

	@Value("${coordinadores.topic}")
	private String coordinadoresTopic;

	@Value("${contador.topic}")
	private String contadorTopic;

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;


	Map<String, Object> producerConfigs(){
		Map<String,Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
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
    KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        configs.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
        configs.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required ;");
        configs.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30 segundos
        configs.put(AdminClientConfig.RETRIES_CONFIG, 5);
        return new KafkaAdmin(configs);
    }

	@Bean
	ProducerFactory<String, Object>producerFactoryEvento(){
		return new DefaultKafkaProducerFactory<String, Object>(producerConfigs());
	}
	@Bean
	KafkaTemplate<String, Object> kafkaTemplateEvento(){
		return new KafkaTemplate<String, Object>(producerFactoryEvento());
	}
	@Bean("kafkaTransactionManager")
	KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
		return new KafkaTransactionManager<>(producerFactory);
	}
	@Bean("transactionManager")
	JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
	
	@Bean
	NewTopic adminsTopic() {
		return TopicBuilder.name(adminsTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}
	
	@Bean
	NewTopic clientesTopic() {
		return TopicBuilder.name(clientesTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}


	@Bean
	NewTopic organizadorTopic() {
		return TopicBuilder.name(organizadorTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}
	@Bean
	NewTopic promotorTopic() {
		return TopicBuilder.name(promotorTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}

	@Bean
	NewTopic puntoFTopic() {
		return TopicBuilder.name(puntoFTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}

	@Bean
	NewTopic coordinadorTopic() {
		return TopicBuilder.name(coordinadoresTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}

	@Bean
	NewTopic contadorTopic() {
		return TopicBuilder.name(contadorTopic)
				.partitions(3)
				.replicas(3)
				.configs(Map.of("min.insync.replicas","2"))//Minimos numero de replicas que deben reconocer la x operacion para desbloquearse pero la data se guarda mas seguro aunque más lento entre mayor sea el numero
				.build();
	}

}
