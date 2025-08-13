package com.arquitectura.tipo;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.tipo.consumer.TipoEventAdapter;
import com.arquitectura.tipo.consumer.TipoConsumerServiceImpl;
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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.entity.TipoRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        TipoConsumerServiceImpl.class,
        TipoConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "tipos-eventos.topic=" + TipoConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = TipoConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class TipoConsumerTest {

    static final String TOPIC = "tipos-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean private TipoRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private TipoEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de tipo correctamente")
    void consumeTipoEvent_persistsEntity_andRegistersMessage() {
        TipoEvent evt = new TipoEvent();
        evt.setId(1L);
        evt.setNombre("Tipo General");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        Tipo nuevoTipo = new Tipo();
        nuevoTipo.setId(evt.getId());
        nuevoTipo.setNombre(evt.getNombre());
        given(adapter.creacion(any(Tipo.class), eq(evt))).willReturn(nuevoTipo);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Tipo.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de tipo existente")
    void consumeTipoEvent_updatesExistingEntity_andRegistersMessage() {
        TipoEvent evt = new TipoEvent();
        evt.setId(1L);
        evt.setNombre("Tipo VIP");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        Tipo tipoExistente = new Tipo();
        tipoExistente.setId(evt.getId());
        tipoExistente.setNombre("Tipo General");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(tipoExistente));

        Tipo tipoActualizado = new Tipo();
        tipoActualizado.setId(evt.getId());
        tipoActualizado.setNombre(evt.getNombre());
        given(adapter.creacion(eq(tipoExistente), eq(evt))).willReturn(tipoActualizado);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Tipo.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeTipoEvent_skipsDuplicateMessage() {
        TipoEvent evt = new TipoEvent();
        evt.setId(1L);
        evt.setNombre("Tipo Duplicado");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(true);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Tipo.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Tipo.class), any(TipoEvent.class));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Tipo tipoExistente = new Tipo();
        tipoExistente.setId(delEvt.getId());
        tipoExistente.setNombre("Tipo a Eliminar");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(tipoExistente));

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(tipoExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de tipo no existente")
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
