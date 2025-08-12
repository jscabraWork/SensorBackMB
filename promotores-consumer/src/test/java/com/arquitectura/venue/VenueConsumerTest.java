package com.arquitectura.venue;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.VenueEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.venue.consumer.VenueEventAdapter;
import com.arquitectura.venue.consumer.VenueServiceImpl;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
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
        VenueServiceImpl.class,
        VenueConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "venues.topic=" + VenueConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = VenueConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class VenueConsumerTest {

    static final String TOPIC = "venues-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean private VenueRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private VenueEventAdapter adapter;

    @BeforeEach
    void setUpKafkaTemplate() {
        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        ProducerFactory<String, BaseEvent> pf =
            new DefaultKafkaProducerFactory<>(props,
                    new StringSerializer(),
                    new JsonSerializer<>());
        kafkaTemplate = new KafkaTemplate<>(pf);
    }

    @Test
    @DisplayName("Debe procesar evento de creación de venue correctamente")
    void consumeVenueEvent_persistsEntity_andRegistersMessage() {
        VenueEvent evt = new VenueEvent();
        evt.setId(1L);
        evt.setNombre("Estadio El Campín");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        Venue nuevoVenue = new Venue();
        nuevoVenue.setId(evt.getId());
        nuevoVenue.setNombre(evt.getNombre());
        given(adapter.creacion(any(Venue.class), eq(evt))).willReturn(nuevoVenue);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Venue.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de venue existente")
    void consumeVenueEvent_updatesExistingEntity_andRegistersMessage() {
        VenueEvent evt = new VenueEvent();
        evt.setId(1L);
        evt.setNombre("Estadio Atanasio Girardot");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        Venue venueExistente = new Venue();
        venueExistente.setId(evt.getId());
        venueExistente.setNombre("Estadio El Campín");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(venueExistente));

        Venue venueActualizado = new Venue();
        venueActualizado.setId(evt.getId());
        venueActualizado.setNombre(evt.getNombre());
        given(adapter.creacion(eq(venueExistente), eq(evt))).willReturn(venueActualizado);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Venue.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeVenueEvent_skipsDuplicateMessage() {
        VenueEvent evt = new VenueEvent();
        evt.setId(1L);
        evt.setNombre("Venue Duplicado");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(true);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Venue.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Venue.class), any(VenueEvent.class));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Venue venueExistente = new Venue();
        venueExistente.setId(delEvt.getId());
        venueExistente.setNombre("Venue a Eliminar");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(venueExistente));

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(venueExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de venue no existente")
    void consumeDeleteEvent_handlesMissingEntity() {
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(999L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.empty());

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).deleteById(anyLong());
            verify(messageService, never()).crearMensaje(anyString(), anyString());
        });
    }

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
            factory.setCommonErrorHandler(new DefaultErrorHandler());
            return factory;
        }
    }
}
