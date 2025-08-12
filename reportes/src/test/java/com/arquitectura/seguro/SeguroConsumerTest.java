package com.arquitectura.seguro;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.SeguroEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.seguro.consumer.SeguroEventAdapter;
import com.arquitectura.seguro.consumer.SeguroConsumerServiceImpl;
import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.seguro.entity.SeguroRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@EnableKafka
@SpringBootTest(
    classes = {
        SeguroConsumerServiceImpl.class,
        SeguroConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "seguros.topic=" + SeguroConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = SeguroConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class SeguroConsumerTest {

    static final String TOPIC = "seguros-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private SeguroRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private SeguroEventAdapter adapter;

    @BeforeEach
    void setUpKafkaTemplate() {
        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        ProducerFactory<String, BaseEvent> pf =
            new DefaultKafkaProducerFactory<>(props,
                    new StringSerializer(),
                    new JsonSerializer<>());
        kafkaTemplate = new KafkaTemplate<>(pf);
    }

    /* ────────────────── CASO CREACIÓN ────────────────── */
    @Test
    @DisplayName("Debe procesar evento de creación de seguro correctamente")
    void consumeSeguroEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        SeguroEvent evt = new SeguroEvent();
        evt.setId(1L);
        evt.setValor(25000.0);
        evt.setReclamado(false);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo un Seguro coherente
        Seguro nuevoSeguro = new Seguro();
        nuevoSeguro.setId(evt.getId());
        nuevoSeguro.setValor(evt.getValor());
        nuevoSeguro.setReclamado(evt.isReclamado());
        given(adapter.creacion(any(Seguro.class), eq(evt))).willReturn(nuevoSeguro);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Seguro.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de seguro existente")
    void consumeSeguroEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        SeguroEvent evt = new SeguroEvent();
        evt.setId(1L);
        evt.setValor(50000.0);
        evt.setReclamado(true);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Seguro existente en BD
        Seguro seguroExistente = new Seguro();
        seguroExistente.setId(evt.getId());
        seguroExistente.setValor(25000.0);
        seguroExistente.setReclamado(false);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(seguroExistente));

        // Mock adapter actualizando el seguro existente
        Seguro seguroActualizado = new Seguro();
        seguroActualizado.setId(evt.getId());
        seguroActualizado.setValor(evt.getValor());
        seguroActualizado.setReclamado(evt.isReclamado());
        given(adapter.creacion(eq(seguroExistente), eq(evt))).willReturn(seguroActualizado);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Seguro.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeSeguroEvent_skipsDuplicateMessage() {
        // Arrange
        SeguroEvent evt = new SeguroEvent();
        evt.setId(1L);
        evt.setValor(25000.0);
        evt.setReclamado(false);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje ya fue procesado
        given(messageService.existeMessage(msgId)).willReturn(true);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - No debe llamar al repositorio ni crear mensaje
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Seguro.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Seguro.class), any(SeguroEvent.class));
        });
    }

    /* ────────────────── CASO ELIMINACIÓN ─────────────── */
    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        // Arrange
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Seguro seguroExistente = new Seguro();
        seguroExistente.setId(delEvt.getId());
        seguroExistente.setValor(25000.0);
        seguroExistente.setReclamado(false);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(seguroExistente));

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(delEvt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de seguro no existente")
    void consumeDeleteEvent_handlesMissingEntity() {
        // Arrange
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(999L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.empty());

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - No debe eliminar ni crear mensaje
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).deleteById(anyLong());
            verify(messageService, never()).crearMensaje(anyString(), anyString());
        });
    }

    /* ────────────────── Configuración Kafka mínima ────── */
    @TestConfiguration
    static class KafkaTestConfig {

        @Bean("kafkaListenerContainerFactory")
        ConcurrentKafkaListenerContainerFactory<String, BaseEvent> factory(
                EmbeddedKafkaBroker broker) {

            Map<String, Object> props = KafkaTestUtils.consumerProps(
                    "test-group", "false", broker);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.arquitectura.*");

            ErrorHandlingDeserializer<BaseEvent> valDeser = new ErrorHandlingDeserializer<>(
                    new JsonDeserializer<>(BaseEvent.class));

            ConsumerFactory<String, BaseEvent> cf =
                    new DefaultKafkaConsumerFactory<>(
                            props, new StringDeserializer(), valDeser);

            ConcurrentKafkaListenerContainerFactory<String, BaseEvent> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(cf);
            factory.setCommonErrorHandler(new DefaultErrorHandler()); // simple logging
            return factory;
        }
    }
}
