package com.arquitectura.alcancia;

import com.arquitectura.alcancia.consumer.AlcanciaConsumerServiceImpl;
import com.arquitectura.alcancia.consumer.AlcanciaEventAdapter;
import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@EnableKafka
@SpringBootTest(
    classes = {
        AlcanciaConsumerServiceImpl.class,
        AlcanciaConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "alcancias.topic=" + AlcanciaConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = AlcanciaConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class AlcanciaConsumerTest {

    static final String TOPIC = "alcancias-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private AlcanciaRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private AlcanciaEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de alcancia correctamente")
    void consumeAlcanciaEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        AlcanciaEvent evt = new AlcanciaEvent();
        evt.setId(1L);
        evt.setPrecioParcialPagado(50000.0);
        evt.setPrecioTotal(100000.0);
        evt.setActiva(true);
        evt.setClienteNumeroDocumento("12345678");
        evt.setTicketsIds(List.of(1L, 2L));

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una Alcancia coherente
        Alcancia nuevaAlcancia = new Alcancia();
        nuevaAlcancia.setId(evt.getId());
        nuevaAlcancia.setPrecioParcialPagado(evt.getPrecioParcialPagado());
        nuevaAlcancia.setPrecioTotal(evt.getPrecioTotal());
        nuevaAlcancia.setActiva(evt.isActiva());
        given(adapter.creacion(any(Alcancia.class), eq(evt))).willReturn(nuevaAlcancia);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Alcancia.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de alcancia existente")
    void consumeAlcanciaEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        AlcanciaEvent evt = new AlcanciaEvent();
        evt.setId(1L);
        evt.setPrecioParcialPagado(75000.0);
        evt.setPrecioTotal(150000.0);
        evt.setActiva(false);
        evt.setClienteNumeroDocumento("87654321");
        evt.setTicketsIds(List.of(3L, 4L));

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Alcancia existente en BD
        Alcancia alcanciaExistente = new Alcancia();
        alcanciaExistente.setId(evt.getId());
        alcanciaExistente.setPrecioParcialPagado(50000.0);
        alcanciaExistente.setPrecioTotal(100000.0);
        alcanciaExistente.setActiva(true);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(alcanciaExistente));

        // Mock adapter actualizando la alcancia existente
        Alcancia alcanciaActualizada = new Alcancia();
        alcanciaActualizada.setId(evt.getId());
        alcanciaActualizada.setPrecioParcialPagado(evt.getPrecioParcialPagado());
        alcanciaActualizada.setPrecioTotal(evt.getPrecioTotal());
        alcanciaActualizada.setActiva(evt.isActiva());
        given(adapter.creacion(eq(alcanciaExistente), eq(evt))).willReturn(alcanciaActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Alcancia.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeAlcanciaEvent_skipsDuplicateMessage() {
        // Arrange
        AlcanciaEvent evt = new AlcanciaEvent();
        evt.setId(1L);
        evt.setPrecioParcialPagado(50000.0);
        evt.setPrecioTotal(100000.0);
        evt.setActiva(true);

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
            verify(repository, never()).save(any(Alcancia.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Alcancia.class), any(AlcanciaEvent.class));
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

        Alcancia alcanciaExistente = new Alcancia();
        alcanciaExistente.setId(delEvt.getId());
        alcanciaExistente.setPrecioParcialPagado(50000.0);
        alcanciaExistente.setPrecioTotal(100000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(alcanciaExistente));

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(alcanciaExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de alcancia no existente")
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
