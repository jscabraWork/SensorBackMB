package com.arquitectura.imagen;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.aws.AWSS3Service;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.imagen.service.ImagenService;
import com.arquitectura.temporada.entity.Temporada;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class ImagenServiceTest {

    @MockBean
    private ImagenRepository imagenRepository;

    @MockBean
    private AWSS3Service awsService;

    @MockBean
    private EventoService eventoService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ImagenService imagenService;

    private Evento evento;
    private Imagen imagen;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        Temporada temporada = Temporada.builder()
                .id(1L)
                .nombre("Temporada Test")
                .build();

        evento = Evento.builder()
                .id(1L)
                .nombre("Evento Test")
                .temporada(temporada)
                .build();

        imagen = new Imagen("test.jpg", 1, evento);
        imagen.setId(1L);

        mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "contenido test".getBytes()
        );
    }

    @Test
    void testCrearImagenExitoso() throws Exception {
        // Arrange
        when(awsService.uploadFile(mockFile)).thenReturn("uploaded-test.jpg");
        when(eventoService.findById(1L)).thenReturn(evento);
        when(imagenRepository.save(any(Imagen.class))).thenReturn(imagen);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        Imagen resultado = imagenService.crear(mockFile, 1L, 1);

        // Assert
        assertNotNull(resultado);
        assertEquals(imagen.getId(), resultado.getId());
        verify(awsService, times(1)).uploadFile(mockFile);
        verify(eventoService, times(1)).findById(1L);
        verify(imagenRepository, times(2)).save(any(Imagen.class)); // Una vez en crear, otra en saveKafka
    }

    @Test
    void testCrearImagenTipo4NoEnviaKafka() throws Exception {
        // Arrange
        when(awsService.uploadFile(mockFile)).thenReturn("uploaded-test.jpg");
        when(eventoService.findById(1L)).thenReturn(evento);
        when(imagenRepository.save(any(Imagen.class))).thenReturn(imagen);

        // Act
        Imagen resultado = imagenService.crear(mockFile, 1L, 4);

        // Assert
        assertNotNull(resultado);
        verify(awsService, times(1)).uploadFile(mockFile);
        verify(eventoService, times(1)).findById(1L);
        verify(imagenRepository, times(1)).save(any(Imagen.class)); // Solo una vez en crear
        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void testCrearImagenErrorAWS() throws Exception {
        // Arrange
        when(awsService.uploadFile(mockFile)).thenThrow(new RuntimeException("Error AWS"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> imagenService.crear(mockFile, 1L, 1));

        verify(awsService, times(1)).uploadFile(mockFile);
        verify(eventoService, never()).findById(any());
        verify(imagenRepository, never()).save(any());
    }

    @Test
    void testSaveKafkaExitoso() throws Exception {
        // Arrange
        when(imagenRepository.save(imagen)).thenReturn(imagen);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        Imagen resultado = imagenService.saveKafka(imagen);

        // Assert
        assertNotNull(resultado);
        assertEquals(imagen.getId(), resultado.getId());
        verify(imagenRepository, times(1)).save(imagen);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void testDeleteByIdExitoso() {
        // Arrange
        imagen.setUrl("https://marcablanca.allticketscol.com/test.jpg");
        when(imagenRepository.findById(1L)).thenReturn(Optional.of(imagen));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        imagenService.deleteById(1L);

        // Assert
        verify(imagenRepository, times(1)).findById(1L);
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
        verify(awsService, times(1)).deleteFile("test.jpg");
        verify(imagenRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteByIdImagenNoExiste() {
        // Arrange
        when(imagenRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> imagenService.deleteById(1L));

        verify(imagenRepository, times(1)).findById(1L);
        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
        verify(awsService, never()).deleteFile(any());
        verify(imagenRepository, never()).deleteById(any());
    }

    @Test
    void testFindByIdExitoso() {
        // Arrange
        when(imagenRepository.findById(1L)).thenReturn(Optional.of(imagen));

        // Act
        Imagen resultado = imagenService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(imagen.getId(), resultado.getId());
        verify(imagenRepository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        // Arrange
        when(imagenRepository.save(imagen)).thenReturn(imagen);

        // Act
        Imagen resultado = imagenService.save(imagen);

        // Assert
        assertNotNull(resultado);
        assertEquals(imagen.getId(), resultado.getId());
        verify(imagenRepository, times(1)).save(imagen);
    }
}
