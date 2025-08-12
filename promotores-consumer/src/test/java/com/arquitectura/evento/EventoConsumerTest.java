package com.arquitectura.evento;

import com.arquitectura.evento.consumer.EventoEventAdapter;
import com.arquitectura.evento.consumer.EventoServiceImpl;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoEvent;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@EnableKafka
@SpringBootTest(
    classes = {
        EventoServiceImpl.class,
        EventoConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "eventos.topic=" + EventoConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = EventoConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class EventoConsumerTest {

    static final String TOPIC = "eventos-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private EventoRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private EventoEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de evento correctamente")
    void consumeEventoEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        EventoEvent evt = new EventoEvent();
        evt.setId(1L);
        evt.setPulep("PULEP-001");
        evt.setArtistas("Los Artistas");
        evt.setNombre("Evento Test");
        evt.setEstado(1);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo un Evento coherente
        Evento nuevoEvento = new Evento();
        nuevoEvento.setId(evt.getId());
        nuevoEvento.setPulep(evt.getPulep());
        nuevoEvento.setArtistas(evt.getArtistas());
        nuevoEvento.setNombre(evt.getNombre());
        nuevoEvento.setEstado(evt.getEstado());
        given(adapter.creacion(any(Evento.class), eq(evt))).willReturn(nuevoEvento);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Evento.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de evento existente")
    void consumeEventoEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        EventoEvent evt = new EventoEvent();
        evt.setId(1L);
        evt.setPulep("PULEP-002");
        evt.setArtistas("Artistas Actualizados");
        evt.setNombre("Evento Actualizado");
        evt.setEstado(1);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Evento existente en BD
        Evento eventoExistente = new Evento();
        eventoExistente.setId(evt.getId());
        eventoExistente.setPulep("PULEP-001");
        eventoExistente.setNombre("Evento Original");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(eventoExistente));

        // Mock adapter actualizando el evento existente
        Evento eventoActualizado = new Evento();
        eventoActualizado.setId(evt.getId());
        eventoActualizado.setPulep(evt.getPulep());
        eventoActualizado.setArtistas(evt.getArtistas());
        eventoActualizado.setNombre(evt.getNombre());
        eventoActualizado.setEstado(evt.getEstado());
        given(adapter.creacion(eq(eventoExistente), eq(evt))).willReturn(eventoActualizado);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Evento.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeEventoEvent_skipsDuplicateMessage() {
        // Arrange
        EventoEvent evt = new EventoEvent();
        evt.setId(1L);
        evt.setNombre("Evento Test");

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
            verify(repository, never()).save(any(Evento.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
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

        Evento eventoExistente = new Evento();
        eventoExistente.setId(delEvt.getId());
        eventoExistente.setNombre("Evento a Eliminar");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(eventoExistente));

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(eventoExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de evento no existente")
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
