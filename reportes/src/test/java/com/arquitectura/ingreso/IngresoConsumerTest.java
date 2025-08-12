package com.arquitectura.ingreso;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.ingreso.consumer.IngresoConsumerServiceImpl;
import com.arquitectura.ingreso.consumer.IngresoEventAdapter;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import com.arquitectura.message.service.MessageService;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@EnableKafka
@SpringBootTest(
    classes = {
        IngresoConsumerServiceImpl.class,
        IngresoConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "ingresos.topic=" + IngresoConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = IngresoConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class IngresoConsumerTest {

    static final String TOPIC = "ingresos-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private IngresoRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private IngresoEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de ingreso correctamente")
    void consumeIngresoEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        IngresoEvent evt = new IngresoEvent();
        evt.setId(1L);
        evt.setUtilizado(true);
        evt.setTicketId(100L);
        evt.setDiaId(10L);
        evt.setFechaIngreso(LocalDateTime.now());

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo un Ingreso coherente
        Ingreso nuevoIngreso = new Ingreso();
        nuevoIngreso.setId(evt.getId());
        nuevoIngreso.setUtilizado(evt.isUtilizado());
        nuevoIngreso.setFechaIngreso(evt.getFechaIngreso());
        given(adapter.creacion(any(Ingreso.class), eq(evt))).willReturn(nuevoIngreso);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Ingreso.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de ingreso existente")
    void consumeIngresoEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        IngresoEvent evt = new IngresoEvent();
        evt.setId(1L);
        evt.setUtilizado(false);
        evt.setTicketId(200L);
        evt.setDiaId(20L);
        evt.setFechaIngreso(LocalDateTime.now().minusHours(1));

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Ingreso existente en BD
        Ingreso ingresoExistente = new Ingreso();
        ingresoExistente.setId(evt.getId());
        ingresoExistente.setUtilizado(true);
        ingresoExistente.setFechaIngreso(LocalDateTime.now().minusDays(1));

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(ingresoExistente));

        // Mock adapter actualizando el ingreso existente
        Ingreso ingresoActualizado = new Ingreso();
        ingresoActualizado.setId(evt.getId());
        ingresoActualizado.setUtilizado(evt.isUtilizado());
        ingresoActualizado.setFechaIngreso(evt.getFechaIngreso());
        given(adapter.creacion(eq(ingresoExistente), eq(evt))).willReturn(ingresoActualizado);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Ingreso.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeIngresoEvent_skipsDuplicateMessage() {
        // Arrange
        IngresoEvent evt = new IngresoEvent();
        evt.setId(1L);
        evt.setUtilizado(true);
        evt.setTicketId(100L);
        evt.setDiaId(10L);
        evt.setFechaIngreso(LocalDateTime.now());

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
            verify(repository, never()).save(any(Ingreso.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Ingreso.class), any(IngresoEvent.class));
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

        Ingreso ingresoExistente = new Ingreso();
        ingresoExistente.setId(delEvt.getId());
        ingresoExistente.setUtilizado(true);
        ingresoExistente.setFechaIngreso(LocalDateTime.now());

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(ingresoExistente));

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
    @DisplayName("Debe manejar eliminación de ingreso no existente")
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
