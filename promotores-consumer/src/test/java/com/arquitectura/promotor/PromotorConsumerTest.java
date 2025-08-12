package com.arquitectura.promotor;

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.promotor.consumer.PromotorEventAdaparter;
import com.arquitectura.promotor.consumer.PromotorServiceConsumerImpl;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.entity.PromotorRepository;
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
        PromotorServiceConsumerImpl.class,
        PromotorConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "promotor.topic=" + PromotorConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = PromotorConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class PromotorConsumerTest {

    static final String TOPIC = "promotor.topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    /* ─── Mocks inyectados ─────────────────────────── */
    @MockitoBean private PromotorRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private PromotorEventAdaparter adapter;

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
    @DisplayName("Debe procesar evento de creación de promotor correctamente")
    void consumeUsuarioEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("12345678");
        evt.setNombre("Juan Pérez");
        evt.setCorreo("juan.perez@email.com");
        evt.setTipoDocumentoId(1L);
        evt.setTipoDocumento("CC");
        evt.setCelular("3001234567");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId();

        // El mensaje aún no se ha procesado
        given(messageService.existeMessage(msgId)).willReturn(false);
        // No existe previamente en BD
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        // Mock adapter devolviendo un Promotor coherente
        Promotor nuevoPromotor = new Promotor();
        nuevoPromotor.setNumeroDocumento(evt.getId());
        nuevoPromotor.setNombre(evt.getNombre());
        nuevoPromotor.setCorreo(evt.getCorreo());
        nuevoPromotor.setTipoDocumento(evt.getTipoDocumento());
        nuevoPromotor.setCelular(evt.getCelular());
        given(adapter.creacion(any(Promotor.class), eq(evt))).willReturn(nuevoPromotor);

        // Act - Publicamos en Kafka
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert - Verificamos que se guarde y registre el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Promotor.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de promotor existente")
    void consumeUsuarioEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("12345678");
        evt.setNombre("Juan Pérez - Actualizado");
        evt.setCorreo("juan.perez.updated@email.com");
        evt.setTipoDocumentoId(1L);
        evt.setTipoDocumento("CC");
        evt.setCelular("3007654321");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId();

        // Promotor existente en BD
        Promotor promotorExistente = new Promotor();
        promotorExistente.setNumeroDocumento(evt.getId());
        promotorExistente.setNombre("Juan Pérez");
        promotorExistente.setCorreo("juan.perez@email.com");
        promotorExistente.setTipoDocumento("CC");
        promotorExistente.setCelular("3001234567");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(promotorExistente));

        // Mock adapter actualizando el promotor existente
        Promotor promotorActualizado = new Promotor();
        promotorActualizado.setNumeroDocumento(evt.getId());
        promotorActualizado.setNombre(evt.getNombre());
        promotorActualizado.setCorreo(evt.getCorreo());
        promotorActualizado.setTipoDocumento(evt.getTipoDocumento());
        promotorActualizado.setCelular(evt.getCelular());
        given(adapter.creacion(eq(promotorExistente), eq(evt))).willReturn(promotorActualizado);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Promotor.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeUsuarioEvent_skipsDuplicateMessage() {
        // Arrange
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("12345678");
        evt.setNombre("Juan Pérez");
        evt.setCorreo("juan.perez@email.com");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId();

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
            verify(repository, never()).save(any(Promotor.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Promotor.class), any(UsuarioEvent.class));
        });
    }

    /* ────────────────── CASO ELIMINACIÓN ─────────────── */
    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        // Arrange
        EntityDeleteEventString delEvt = new EntityDeleteEventString();
        delEvt.setId("12345678");

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId();

        Promotor promotorExistente = new Promotor();
        promotorExistente.setNumeroDocumento(delEvt.getId());
        promotorExistente.setNombre("Juan Pérez");
        promotorExistente.setCorreo("juan.perez@email.com");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(promotorExistente));

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(messageService).crearMensaje(eq(msgId), eq(delEvt.getId()));
            verify(repository).deleteById(delEvt.getId());
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de promotor no existente")
    void consumeDeleteEvent_handlesMissingEntity() {
        // Arrange
        EntityDeleteEventString delEvt = new EntityDeleteEventString();
        delEvt.setId("99999999");

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId();

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
            verify(repository, never()).deleteById(anyString());
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
