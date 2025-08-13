package com.arquitectura.temporada;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.temporada.entity.TemporadaRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        TemporadaServiceImpl.class,
        TemporadaConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "temporadas.topic=" + TemporadaConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = TemporadaConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class TemporadaConsumerTest {

    static final String TOPIC = "temporadas-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private TemporadaRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private TemporadaEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de temporada correctamente")
    void consumeTemporadaEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        TemporadaEvent evt = new TemporadaEvent();
        evt.setId(1L);
        evt.setNombre("Temporada Verano 2024");
        evt.setFechaInicio(LocalDateTime.of(2024, 6, 1, 0, 0));
        evt.setFechaFin(LocalDateTime.of(2024, 8, 31, 23, 59));
        evt.setEstado(1); // ACTIVA

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una Temporada coherente
        Temporada nuevaTemporada = new Temporada();
        nuevaTemporada.setId(evt.getId());
        nuevaTemporada.setNombre(evt.getNombre());
        nuevaTemporada.setFechaInicio(evt.getFechaInicio());
        nuevaTemporada.setFechaFin(evt.getFechaFin());
        nuevaTemporada.setEstado(evt.getEstado());
        given(adapter.creacion(any(Temporada.class), eq(evt))).willReturn(nuevaTemporada);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Temporada.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de temporada existente")
    void consumeTemporadaEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        TemporadaEvent evt = new TemporadaEvent();
        evt.setId(1L);
        evt.setNombre("Temporada Verano 2024 - Actualizada");
        evt.setFechaInicio(LocalDateTime.of(2024, 6, 15, 0, 0));
        evt.setFechaFin(LocalDateTime.of(2024, 9, 15, 23, 59));
        evt.setEstado(2); // INACTIVA

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Temporada existente en BD
        Temporada temporadaExistente = new Temporada();
        temporadaExistente.setId(evt.getId());
        temporadaExistente.setNombre("Temporada Verano 2024");
        temporadaExistente.setFechaInicio(LocalDateTime.of(2024, 6, 1, 0, 0));
        temporadaExistente.setFechaFin(LocalDateTime.of(2024, 8, 31, 23, 59));
        temporadaExistente.setEstado(1);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(temporadaExistente));

        // Mock adapter actualizando la temporada existente
        Temporada temporadaActualizada = new Temporada();
        temporadaActualizada.setId(evt.getId());
        temporadaActualizada.setNombre(evt.getNombre());
        temporadaActualizada.setFechaInicio(evt.getFechaInicio());
        temporadaActualizada.setFechaFin(evt.getFechaFin());
        temporadaActualizada.setEstado(evt.getEstado());
        given(adapter.creacion(eq(temporadaExistente), eq(evt))).willReturn(temporadaActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Temporada.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeTemporadaEvent_skipsDuplicateMessage() {
        // Arrange
        TemporadaEvent evt = new TemporadaEvent();
        evt.setId(1L);
        evt.setNombre("Temporada Verano 2024");
        evt.setFechaInicio(LocalDateTime.of(2024, 6, 1, 0, 0));
        evt.setFechaFin(LocalDateTime.of(2024, 8, 31, 23, 59));
        evt.setEstado(1);

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
            verify(repository, never()).save(any(Temporada.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Temporada.class), any(TemporadaEvent.class));
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

        Temporada temporadaExistente = new Temporada();
        temporadaExistente.setId(delEvt.getId());
        temporadaExistente.setNombre("Temporada Verano 2024");
        temporadaExistente.setEstado(1);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(temporadaExistente));

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
    @DisplayName("Debe manejar eliminación de temporada no existente")
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
