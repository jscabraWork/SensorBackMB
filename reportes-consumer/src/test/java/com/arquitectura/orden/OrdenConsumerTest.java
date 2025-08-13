
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.orden.consumer.OrdenEventAdapter;
import com.arquitectura.orden.consumer.OrdenConsumerServiceImpl;
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

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        OrdenConsumerServiceImpl.class,
        OrdenConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "ordenes.topic=" + OrdenConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = OrdenConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class OrdenConsumerTest {

    static final String TOPIC = "ordenes-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private OrdenRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private OrdenEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de orden correctamente")
    void consumeOrdenEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        OrdenEvent evt = new OrdenEvent();
        evt.setId(1L);
        evt.setEstado(1); // ACEPTADA
        evt.setTipo(1); // COMPRA ESTANDAR
        evt.setEventoId(100L);
        evt.setValorOrden(150000.0);
        evt.setValorSeguro(5000.0);
        evt.setTicketsIds(Arrays.asList(1L, 2L, 3L));
        evt.setClienteId("12345678");
        evt.setTarifaId(10L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una Orden coherente
        Orden nuevaOrden = new Orden();
        nuevaOrden.setId(evt.getId());
        nuevaOrden.setEstado(evt.getEstado());
        nuevaOrden.setTipo(evt.getTipo());
        nuevaOrden.setValorOrden(evt.getValorOrden());
        nuevaOrden.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(any(Orden.class), eq(evt))).willReturn(nuevaOrden);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Orden.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de orden existente")
    void consumeOrdenEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        OrdenEvent evt = new OrdenEvent();
        evt.setId(1L);
        evt.setEstado(2); // RECHAZADA
        evt.setTipo(2); // ADICIONES
        evt.setEventoId(200L);
        evt.setValorOrden(250000.0);
        evt.setValorSeguro(8000.0);
        evt.setTicketsIds(Arrays.asList(4L, 5L));
        evt.setClienteId("87654321");
        evt.setTarifaId(20L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Orden existente en BD
        Orden ordenExistente = new Orden();
        ordenExistente.setId(evt.getId());
        ordenExistente.setEstado(1);
        ordenExistente.setTipo(1);
        ordenExistente.setValorOrden(150000.0);
        ordenExistente.setValorSeguro(5000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(ordenExistente));

        // Mock adapter actualizando la orden existente
        Orden ordenActualizada = new Orden();
        ordenActualizada.setId(evt.getId());
        ordenActualizada.setEstado(evt.getEstado());
        ordenActualizada.setTipo(evt.getTipo());
        ordenActualizada.setValorOrden(evt.getValorOrden());
        ordenActualizada.setValorSeguro(evt.getValorSeguro());
        given(adapter.creacion(eq(ordenExistente), eq(evt))).willReturn(ordenActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Orden.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeOrdenEvent_skipsDuplicateMessage() {
        // Arrange
        OrdenEvent evt = new OrdenEvent();
        evt.setId(1L);
        evt.setEstado(1);
        evt.setTipo(1);
        evt.setValorOrden(150000.0);

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
            verify(repository, never()).save(any(Orden.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Orden.class), any(OrdenEvent.class));
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

        Orden ordenExistente = new Orden();
        ordenExistente.setId(delEvt.getId());
        ordenExistente.setEstado(1);
        ordenExistente.setValorOrden(150000.0);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(ordenExistente));

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
    @DisplayName("Debe manejar eliminación de orden no existente")
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
