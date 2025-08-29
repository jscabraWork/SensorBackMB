package com.arquitectura.orden_puntofisico;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisicoRepository;
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
        OrdenConsumerPuntoFisicoServiceImpl.class,
        OrdenPuntoFisicoConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "ordenes.puntosF.topic=" + OrdenPuntoFisicoConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = OrdenPuntoFisicoConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class OrdenPuntoFisicoConsumerTest {

    static final String TOPIC = "ordenes.puntosF.topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private OrdenPuntoFisicoRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private OrdenPuntoFisicoEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de orden punto físico correctamente")
    void consumeOrdenPuntoFisicoEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        OrdenPuntoFisicoEvent evt = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado: ACEPTADA
                7,            // tipo: VENTA PUNTO FISICO
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L, 3L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una OrdenPuntoFisico coherente
        OrdenPuntoFisico nuevaOrdenPuntoFisico = new OrdenPuntoFisico();
        nuevaOrdenPuntoFisico.setId(evt.getId());
        nuevaOrdenPuntoFisico.setEstado(evt.getEstado());
        nuevaOrdenPuntoFisico.setTipo(evt.getTipo());
        nuevaOrdenPuntoFisico.setValorOrden(evt.getValorOrden());
        nuevaOrdenPuntoFisico.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(any(OrdenPuntoFisico.class), eq(evt))).willReturn(nuevaOrdenPuntoFisico);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            verify(repository).save(any(OrdenPuntoFisico.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de orden punto físico existente")
    void consumeOrdenPuntoFisicoEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        OrdenPuntoFisicoEvent evt = new OrdenPuntoFisicoEvent(
                1L,           // id
                2,            // estado: RECHAZADA
                8,            // tipo: DEVOLUCION PUNTO FISICO
                200L,         // eventoId
                250000.0,     // valorOrden
                8000.0,       // valorSeguro
                Arrays.asList(4L, 5L),     // ticketsIds
                "87654321",   // clienteId
                20L,          // tarifaId
                "PF002"       // puntoFisicoId
        );

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // OrdenPuntoFisico existente en BD
        OrdenPuntoFisico ordenPuntoFisicoExistente = new OrdenPuntoFisico();
        ordenPuntoFisicoExistente.setId(evt.getId());
        ordenPuntoFisicoExistente.setEstado(1);
        ordenPuntoFisicoExistente.setTipo(7);
        ordenPuntoFisicoExistente.setValorOrden(150000.0);
        ordenPuntoFisicoExistente.setValorSeguro(5000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(ordenPuntoFisicoExistente));

        // Mock adapter actualizando la orden punto físico existente
        OrdenPuntoFisico ordenPuntoFisicoActualizada = new OrdenPuntoFisico();
        ordenPuntoFisicoActualizada.setId(evt.getId());
        ordenPuntoFisicoActualizada.setEstado(evt.getEstado());
        ordenPuntoFisicoActualizada.setTipo(evt.getTipo());
        ordenPuntoFisicoActualizada.setValorOrden(evt.getValorOrden());
        ordenPuntoFisicoActualizada.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(eq(ordenPuntoFisicoExistente), eq(evt))).willReturn(ordenPuntoFisicoActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            verify(repository).save(any(OrdenPuntoFisico.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeOrdenPuntoFisicoEvent_skipsDuplicateMessage() {
        // Arrange
        OrdenPuntoFisicoEvent evt = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L),     // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

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
            verify(repository, never()).save(any(OrdenPuntoFisico.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(OrdenPuntoFisico.class), any(OrdenPuntoFisicoEvent.class));
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

        OrdenPuntoFisico ordenPuntoFisicoExistente = new OrdenPuntoFisico();
        ordenPuntoFisicoExistente.setId(delEvt.getId());
        ordenPuntoFisicoExistente.setEstado(1);
        ordenPuntoFisicoExistente.setTipo(7);
        ordenPuntoFisicoExistente.setValorOrden(150000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(ordenPuntoFisicoExistente));

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
    @DisplayName("Debe manejar eliminación de orden punto físico no existente")
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