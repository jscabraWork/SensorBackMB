package com.arquitectura.imagen;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.consumer.ImagenEventAdapterImpl;
import com.arquitectura.imagen.entity.Imagen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ImagenEventAdapterTest {

    private ImagenEventAdapterImpl adapter;

    @Mock
    private EventoRepository eventoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new ImagenEventAdapterImpl();
        adapter.setRepository(eventoRepository);
    }

    @Test
    @DisplayName("Debe convertir correctamente ImagenEvent a Imagen para creación")
    void testCreacionDesdeImagenEvent() {
        // Arrange
        ImagenEvent event = new ImagenEvent();
        event.setId(1L);
        event.setNombre("img1.jpg");
        event.setUrl("https://marcablanca.allticketscol.com/img1.jpg");
        event.setTipo(1);
        event.setEventoId(10L);

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setNombre("Evento Test");
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        Imagen imagen = new Imagen();

        // Act
        Imagen result = adapter.creacion(imagen, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("img1.jpg");
        assertThat(result.getUrl()).isEqualTo("https://marcablanca.allticketscol.com/img1.jpg");
        assertThat(result.getTipo()).isEqualTo(1);
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getEvento().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Imagen existente")
    void testActualizacionImagenExistente() {
        // Arrange
        ImagenEvent event = new ImagenEvent();
        event.setId(2L);
        event.setNombre("img2.jpg");
        event.setUrl("https://marcablanca.allticketscol.com/img2.jpg");
        event.setTipo(2);
        event.setEventoId(20L);

        Evento evento = new Evento();
        evento.setId(20L);
        evento.setNombre("Otro Evento");
        when(eventoRepository.findById(20L)).thenReturn(Optional.of(evento));

        Imagen imagenExistente = new Imagen();
        imagenExistente.setId(2L);
        imagenExistente.setNombre("imgAntigua.jpg");
        imagenExistente.setUrl("https://marcablanca.allticketscol.com/imgAntigua.jpg");
        imagenExistente.setTipo(1);
        imagenExistente.setEvento(null);

        // Act
        Imagen result = adapter.creacion(imagenExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getNombre()).isEqualTo("img2.jpg");
        assertThat(result.getUrl()).isEqualTo("https://marcablanca.allticketscol.com/img2.jpg");
        assertThat(result.getTipo()).isEqualTo(2);
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getEvento().getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        ImagenEvent event = new ImagenEvent();
        event.setId(3L);
        event.setNombre(null);
        event.setUrl(null);
        event.setTipo(0);
        event.setEventoId(null);

        when(eventoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Imagen imagen = new Imagen();

        // Act
        Imagen result = adapter.creacion(imagen, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.getUrl()).isNull();
        assertThat(result.getTipo()).isEqualTo(0);
        assertThat(result.getEvento()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto Imagen")
    void testPreservaReferenciaObjeto() {
        // Arrange
        ImagenEvent event = new ImagenEvent();
        event.setId(4L);
        event.setNombre("img4.jpg");
        event.setUrl("https://marcablanca.allticketscol.com/img4.jpg");
        event.setTipo(4);
        event.setEventoId(40L);

        Evento evento = new Evento();
        evento.setId(40L);
        evento.setNombre("Evento 40");
        when(eventoRepository.findById(40L)).thenReturn(Optional.of(evento));

        Imagen imagen = new Imagen();

        // Act
        Imagen result = adapter.creacion(imagen, event);

        // Assert
        assertThat(result).isSameAs(imagen);
    }

    @Test
    @DisplayName("Debe manejar correctamente nombres con caracteres especiales")
    void testNombresConCaracteresEspeciales() {
        // Arrange
        ImagenEvent event = new ImagenEvent();
        event.setId(5L);
        event.setNombre("img-ñ-@.jpg");
        event.setUrl("https://marcablanca.allticketscol.com/img-ñ-@.jpg");
        event.setTipo(1);
        event.setEventoId(50L);

        Evento evento = new Evento();
        evento.setId(50L);
        evento.setNombre("Evento Especial");
        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));

        Imagen imagen = new Imagen();

        // Act
        Imagen result = adapter.creacion(imagen, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("img-ñ-@.jpg");
        assertThat(result.getUrl()).isEqualTo("https://marcablanca.allticketscol.com/img-ñ-@.jpg");
    }
}
