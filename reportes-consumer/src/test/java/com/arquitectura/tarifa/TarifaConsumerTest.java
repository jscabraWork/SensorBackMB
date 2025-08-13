package com.arquitectura.tarifa;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.tarifa.consumer.TarifaEventAdapter;
import com.arquitectura.tarifa.consumer.TarifaConsumerServiceImpl;
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


import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        TarifaConsumerServiceImpl.class,
        TarifaConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "tarifas.topic=" + TarifaConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = TarifaConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class TarifaConsumerTest {

    static final String TOPIC = "tarifas-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean private TarifaRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private TarifaEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de tarifa correctamente")
    void consumeTarifaEvent_persistsEntity_andRegistersMessage() {
        TarifaEvent evt = new TarifaEvent();
        evt.setId(1L);
        evt.setNombre("Tarifa General");
        evt.setPrecio(50000.0);
        evt.setServicio(5000.0);
        evt.setIva(9500.0);
        evt.setEstado(1);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        Tarifa nuevaTarifa = new Tarifa();
        nuevaTarifa.setId(evt.getId());
        nuevaTarifa.setNombre(evt.getNombre());
        nuevaTarifa.setPrecio(evt.getPrecio());
        nuevaTarifa.setServicio(evt.getServicio());
        nuevaTarifa.setIva(evt.getIva());
        nuevaTarifa.setEstado(evt.getEstado());
        given(adapter.creacion(any(Tarifa.class), eq(evt))).willReturn(nuevaTarifa);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Tarifa.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de tarifa existente")
    void consumeTarifaEvent_updatesExistingEntity_andRegistersMessage() {
        TarifaEvent evt = new TarifaEvent();
        evt.setId(1L);
        evt.setNombre("Tarifa VIP");
        evt.setPrecio(100000.0);
        evt.setServicio(10000.0);
        evt.setIva(19000.0);
        evt.setEstado(1);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        Tarifa tarifaExistente = new Tarifa();
        tarifaExistente.setId(evt.getId());
        tarifaExistente.setNombre("Tarifa General");
        tarifaExistente.setPrecio(50000.0);
        tarifaExistente.setServicio(5000.0);
        tarifaExistente.setIva(9500.0);
        tarifaExistente.setEstado(0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(tarifaExistente));

        Tarifa tarifaActualizada = new Tarifa();
        tarifaActualizada.setId(evt.getId());
        tarifaActualizada.setNombre(evt.getNombre());
        tarifaActualizada.setPrecio(evt.getPrecio());
        tarifaActualizada.setServicio(evt.getServicio());
        tarifaActualizada.setIva(evt.getIva());
        tarifaActualizada.setEstado(evt.getEstado());
        given(adapter.creacion(eq(tarifaExistente), eq(evt))).willReturn(tarifaActualizada);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Tarifa.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeTarifaEvent_skipsDuplicateMessage() {
        TarifaEvent evt = new TarifaEvent();
        evt.setId(1L);
        evt.setNombre("Tarifa Duplicada");
        evt.setEstado(1);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(true);

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Tarifa.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Tarifa.class), any(TarifaEvent.class));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Tarifa tarifaExistente = new Tarifa();
        tarifaExistente.setId(delEvt.getId());
        tarifaExistente.setNombre("Tarifa a Eliminar");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(tarifaExistente));

        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(tarifaExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de tarifa no existente")
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
