package com.arquitectura.orden_alcancia;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenAlcanciaEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.orden_alcancia.entity.OrdenAlcanciaRepository;
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
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@EnableKafka
@SpringBootTest(
    classes = {
        OrdenConsumerAlcanciaServiceImpl.class,
        OrdenAlcanciaConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "ordenes.alcancias.topic=" + OrdenAlcanciaConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = OrdenAlcanciaConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class OrdenAlcanciaConsumerTest {

    static final String TOPIC = "ordenes.alcancias-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private OrdenAlcanciaRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private OrdenAlcanciaEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de orden alcancía correctamente")
    void consumeOrdenAlcanciaEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        OrdenAlcanciaEvent evt = new OrdenAlcanciaEvent();
        evt.setId(1L);
        evt.setEstado(1); // ACEPTADA
        evt.setTipo(3); // CREAR ALCANCIA
        evt.setEventoId(100L);
        evt.setValorOrden(150000.0);
        evt.setValorSeguro(5000.0);
        evt.setTicketsIds(Arrays.asList(1L, 2L));
        evt.setClienteId("12345678");
        evt.setTarifaId(10L);
        evt.setAlcanciaId(50L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una OrdenAlcancia coherente
        OrdenAlcancia nuevaOrdenAlcancia = new OrdenAlcancia();
        nuevaOrdenAlcancia.setId(evt.getId());
        nuevaOrdenAlcancia.setEstado(evt.getEstado());
        nuevaOrdenAlcancia.setTipo(evt.getTipo());
        nuevaOrdenAlcancia.setValorOrden(evt.getValorOrden());
        nuevaOrdenAlcancia.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(any(OrdenAlcancia.class), eq(evt))).willReturn(nuevaOrdenAlcancia);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(OrdenAlcancia.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de orden alcancía existente")
    void consumeOrdenAlcanciaEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        OrdenAlcanciaEvent evt = new OrdenAlcanciaEvent();
        evt.setId(1L);
        evt.setEstado(1); // ACEPTADA
        evt.setTipo(4); // APORTAR A ALCANCIA
        evt.setEventoId(200L);
        evt.setValorOrden(250000.0);
        evt.setValorSeguro(8000.0);
        evt.setTicketsIds(Arrays.asList(3L, 4L));
        evt.setClienteId("87654321");
        evt.setTarifaId(20L);
        evt.setAlcanciaId(60L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // OrdenAlcancia existente en BD
        OrdenAlcancia ordenAlcanciaExistente = new OrdenAlcancia();
        ordenAlcanciaExistente.setId(evt.getId());
        ordenAlcanciaExistente.setEstado(3);
        ordenAlcanciaExistente.setTipo(3);
        ordenAlcanciaExistente.setValorOrden(150000.0);
        ordenAlcanciaExistente.setValorSeguro(5000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(ordenAlcanciaExistente));

        // Mock adapter actualizando la orden alcancía existente
        OrdenAlcancia ordenAlcanciaActualizada = new OrdenAlcancia();
        ordenAlcanciaActualizada.setId(evt.getId());
        ordenAlcanciaActualizada.setEstado(evt.getEstado());
        ordenAlcanciaActualizada.setTipo(evt.getTipo());
        ordenAlcanciaActualizada.setValorOrden(evt.getValorOrden());
        ordenAlcanciaActualizada.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(eq(ordenAlcanciaExistente), eq(evt))).willReturn(ordenAlcanciaActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(OrdenAlcancia.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeOrdenAlcanciaEvent_skipsDuplicateMessage() {
        // Arrange
        OrdenAlcanciaEvent evt = new OrdenAlcanciaEvent();
        evt.setId(1L);
        evt.setEstado(1);
        evt.setTipo(3);
        evt.setValorOrden(150000.0);
        evt.setAlcanciaId(50L);

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
            verify(repository, never()).save(any(OrdenAlcancia.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(OrdenAlcancia.class), any(OrdenAlcanciaEvent.class));
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

        OrdenAlcancia ordenAlcanciaExistente = new OrdenAlcancia();
        ordenAlcanciaExistente.setId(delEvt.getId());
        ordenAlcanciaExistente.setEstado(1);
        ordenAlcanciaExistente.setTipo(3);
        ordenAlcanciaExistente.setValorOrden(150000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(ordenAlcanciaExistente));

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
    @DisplayName("Debe manejar eliminación de orden alcancía no existente")
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
