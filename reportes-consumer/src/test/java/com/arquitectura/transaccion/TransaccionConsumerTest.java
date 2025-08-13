package com.arquitectura.transaccion;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.transaccion.consumer.TransaccionEventAdapterImpl;
import com.arquitectura.transaccion.consumer.TransaccionServiceConsumerImpl;
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

import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        TransaccionServiceConsumerImpl.class,
        TransaccionConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group"
    }
)
@EmbeddedKafka(partitions = 1, topics = TransaccionConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class TransaccionConsumerTest {

    static final String TOPIC = "cashless-crear-modificar-transacciones-mb-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private TransaccionRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private TransaccionEventAdapterImpl adapter;

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
    @DisplayName("Debe procesar evento de creación de transacción correctamente")
    void consumeTransaccionEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        TransaccionEvent evt = new TransaccionEvent();
        evt.setId(1L);
        evt.setAmount(150000.0);
        evt.setEmail("test@example.com");
        evt.setFullname("Juan Pérez");
        evt.setIdPasarela("PASARELA123");
        evt.setIdPersona("12345678");
        evt.setIp("192.168.1.1");
        evt.setMetodo(1); // TARJETA CREDITO
        evt.setMetodoNombre("Tarjeta de Crédito");
        evt.setPhone("3001234567");
        evt.setStatus(34); // APROBADA
        evt.setIdBasePasarela("BASE123");
        evt.setOrdenId(100L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo una Transacción coherente
        Transaccion nuevaTransaccion = new Transaccion();
        nuevaTransaccion.setId(evt.getId());
        nuevaTransaccion.setAmount(evt.getAmount());
        nuevaTransaccion.setEmail(evt.getEmail());
        nuevaTransaccion.setFullName(evt.getFullname());
        nuevaTransaccion.setStatus(evt.getStatus());
        given(adapter.creacion(any(Transaccion.class), eq(evt))).willReturn(nuevaTransaccion);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Transaccion.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de transacción existente")
    void consumeTransaccionEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        TransaccionEvent evt = new TransaccionEvent();
        evt.setId(1L);
        evt.setAmount(200000.0);
        evt.setEmail("updated@example.com");
        evt.setFullname("Juan Pérez Actualizado");
        evt.setIdPasarela("PASARELA456");
        evt.setIdPersona("12345678");
        evt.setIp("192.168.1.2");
        evt.setMetodo(2); // PSE
        evt.setMetodoNombre("PSE");
        evt.setPhone("3007654321");
        evt.setStatus(35); // EN PROCESO
        evt.setIdBasePasarela("BASE456");
        evt.setOrdenId(200L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        // Transacción existente en BD
        Transaccion transaccionExistente = new Transaccion();
        transaccionExistente.setId(evt.getId());
        transaccionExistente.setAmount(150000.0);
        transaccionExistente.setEmail("test@example.com");
        transaccionExistente.setStatus(34);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(transaccionExistente));

        // Mock adapter actualizando la transacción existente
        Transaccion transaccionActualizada = new Transaccion();
        transaccionActualizada.setId(evt.getId());
        transaccionActualizada.setAmount(evt.getAmount());
        transaccionActualizada.setEmail(evt.getEmail());
        transaccionActualizada.setStatus(evt.getStatus());
        given(adapter.creacion(eq(transaccionExistente), eq(evt))).willReturn(transaccionActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Transaccion.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeTransaccionEvent_skipsDuplicateMessage() {
        // Arrange
        TransaccionEvent evt = new TransaccionEvent();
        evt.setId(1L);
        evt.setAmount(150000.0);
        evt.setEmail("test@example.com");
        evt.setStatus(34);

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
            verify(repository, never()).save(any(Transaccion.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Transaccion.class), any(TransaccionEvent.class));
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

        Transaccion transaccionExistente = new Transaccion();
        transaccionExistente.setId(delEvt.getId());
        transaccionExistente.setAmount(150000.0);
        transaccionExistente.setEmail("test@example.com");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(transaccionExistente));

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
    @DisplayName("Debe manejar eliminación de transacción no existente")
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
