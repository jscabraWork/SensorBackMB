package com.arquitectura.localidad;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.localidad.consumer.LocalidadEventAdapter;
import com.arquitectura.localidad.consumer.LocalidadConsumerServiceImpl;
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

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        LocalidadConsumerServiceImpl.class,
        LocalidadConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "localidades.topic=" + LocalidadConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = LocalidadConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class LocalidadConsumerTest {

    static final String TOPIC = "localidades-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean private LocalidadRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private LocalidadEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de localidad correctamente")
    void consumeLocalidadEvent_persistsEntity_andRegistersMessage() {
        LocalidadEvent evt = new LocalidadEvent();
        evt.setId(1L);
        evt.setNombre("VIP");
        evt.setTipo(1);
        evt.setDescripcion("Zona VIP");
        evt.setAporteMinimo(10000.00);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        Localidad nuevaLocalidad = new Localidad();
        nuevaLocalidad.setId(evt.getId());
        nuevaLocalidad.setNombre(evt.getNombre());
        nuevaLocalidad.setTipo(evt.getTipo());
        nuevaLocalidad.setAporteMinimo(evt.getAporteMinimo());
        given(adapter.creacion(any(Localidad.class), eq(evt))).willReturn(nuevaLocalidad);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Localidad.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de localidad existente")
    void consumeLocalidadEvent_updatesExistingEntity_andRegistersMessage() {
        LocalidadEvent evt = new LocalidadEvent();
        evt.setId(1L);
        evt.setNombre("PLATEA");
        evt.setTipo(2);
        evt.setDescripcion("Zona Platea");
        evt.setAporteMinimo(20000.00);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        Localidad localidadExistente = new Localidad();
        localidadExistente.setId(evt.getId());
        localidadExistente.setNombre("VIP");
        localidadExistente.setTipo(1);
        localidadExistente.setAporteMinimo(10000.00);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(localidadExistente));

        Localidad localidadActualizada = new Localidad();
        localidadActualizada.setId(evt.getId());
        localidadActualizada.setNombre(evt.getNombre());
        localidadActualizada.setTipo(evt.getTipo());
        localidadActualizada.setAporteMinimo(evt.getAporteMinimo());
        given(adapter.creacion(eq(localidadExistente), eq(evt))).willReturn(localidadActualizada);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Localidad.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeLocalidadEvent_skipsDuplicateMessage() {
        LocalidadEvent evt = new LocalidadEvent();
        evt.setId(1L);
        evt.setNombre("VIP");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(true);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Localidad.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Localidad.class), any(LocalidadEvent.class));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Localidad localidadExistente = new Localidad();
        localidadExistente.setId(delEvt.getId());
        localidadExistente.setNombre("Localidad a Eliminar");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(localidadExistente));

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(localidadExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de localidad no existente")
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
