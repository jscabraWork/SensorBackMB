package com.arquitectura.imagen;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.arquitectura.imagen.consumer.ImagenEventAdapter;
import com.arquitectura.imagen.consumer.ImagenConsumerServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
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

import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.message.service.MessageService;

@EnableKafka
@SpringBootTest(
    classes = {
        ImagenConsumerServiceImpl.class,
        ImagenConsumerTest.KafkaTestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group",
        "imagenes.topic=" + ImagenConsumerTest.TOPIC
    }
)
@EmbeddedKafka(partitions = 1, topics = ImagenConsumerTest.TOPIC, controlledShutdown = true)
@ActiveProfiles("test")
class ImagenConsumerTest {

    static final String TOPIC = "imagenes-topic";

    @Autowired
    private EmbeddedKafkaBroker broker;

    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean private ImagenRepository repository;
    @MockitoBean private MessageService messageService;
    @MockitoBean private ImagenEventAdapter adapter;

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
    @DisplayName("Debe procesar evento de creación de imagen correctamente")
    void consumeImagenEvent_persistsEntity_andRegistersMessage() {
        // Arrange
        ImagenEvent evt = new ImagenEvent();
        evt.setId(1L);
        evt.setNombre("img1.jpg");
        evt.setUrl("https://marcablanca.allticketscol.com/img1.jpg");
        evt.setTipo(1);
        evt.setEventoId(10L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.empty());

        Imagen nuevaImagen = new Imagen();
        nuevaImagen.setId(evt.getId());
        nuevaImagen.setNombre(evt.getNombre());
        nuevaImagen.setUrl(evt.getUrl());
        nuevaImagen.setTipo(evt.getTipo());
        given(adapter.creacion(any(Imagen.class), eq(evt))).willReturn(nuevaImagen);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Imagen.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de imagen existente")
    void consumeImagenEvent_updatesExistingEntity_andRegistersMessage() {
        // Arrange
        ImagenEvent evt = new ImagenEvent();
        evt.setId(1L);
        evt.setNombre("img2.jpg");
        evt.setUrl("https://marcablanca.allticketscol.com/img2.jpg");
        evt.setTipo(2);
        evt.setEventoId(10L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        Imagen imagenExistente = new Imagen();
        imagenExistente.setId(evt.getId());
        imagenExistente.setNombre("img1.jpg");
        imagenExistente.setUrl("https://marcablanca.allticketscol.com/img1.jpg");
        imagenExistente.setTipo(1);

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(evt.getId())).willReturn(Optional.of(imagenExistente));

        Imagen imagenActualizada = new Imagen();
        imagenActualizada.setId(evt.getId());
        imagenActualizada.setNombre(evt.getNombre());
        imagenActualizada.setUrl(evt.getUrl());
        imagenActualizada.setTipo(evt.getTipo());
        given(adapter.creacion(eq(imagenExistente), eq(evt))).willReturn(imagenActualizada);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).save(any(Imagen.class));
            verify(messageService).crearMensaje(eq(msgId), eq(evt.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void consumeImagenEvent_skipsDuplicateMessage() {
        // Arrange
        ImagenEvent evt = new ImagenEvent();
        evt.setId(1L);
        evt.setNombre("img1.jpg");

        String msgId = UUID.randomUUID().toString();
        String messageKey = evt.getId().toString();

        given(messageService.existeMessage(msgId)).willReturn(true);

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, evt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(repository, never()).save(any(Imagen.class));
            verify(messageService, never()).crearMensaje(anyString(), anyString());
            verify(adapter, never()).creacion(any(Imagen.class), any(ImagenEvent.class));
        });
    }

    @Test
    @DisplayName("Debe procesar evento de eliminación correctamente")
    void consumeDeleteEvent_deletesEntity_andRegistersMessage() {
        // Arrange
        EntityDeleteEventLong delEvt = new EntityDeleteEventLong();
        delEvt.setId(1L);

        String msgId = UUID.randomUUID().toString();
        String messageKey = delEvt.getId().toString();

        Imagen imagenExistente = new Imagen();
        imagenExistente.setId(delEvt.getId());
        imagenExistente.setNombre("img1.jpg");

        given(messageService.existeMessage(msgId)).willReturn(false);
        given(repository.findById(delEvt.getId())).willReturn(Optional.of(imagenExistente));

        // Act
        ProducerRecord<String, BaseEvent> rec =
                new ProducerRecord<>(TOPIC, messageKey, delEvt);
        rec.headers().add("messageId", msgId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
        kafkaTemplate.flush();

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(repository).deleteById(delEvt.getId());
            verify(messageService).crearMensaje(eq(msgId), eq(imagenExistente.getId().toString()));
        });
    }

    @Test
    @DisplayName("Debe manejar eliminación de imagen no existente")
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

        // Assert
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
