package com.arquitectura.evento;

import com.arquitectura.evento.consumer.EventoEventAdapterImpl;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.temporada.entity.TemporadaRepository;
import com.arquitectura.venue.entity.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class EventoEventAdapterTest {

    @InjectMocks
    private EventoEventAdapterImpl adapter;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private TemporadaRepository temporadaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar comportamiento de los mocks
        when(venueRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(temporadaRepository.findById(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Debe convertir correctamente EventoEvent a Evento para creación")
    void testCreacionDesdeEventoEvent() {
        // Arrange
        EventoEvent event = new EventoEvent();
        event.setId(1L);
        event.setPulep("PULEP-001");
        event.setArtistas("Los Tigres del Norte");
        event.setNombre("Concierto de Los Tigres");
        event.setEstado(1);

        Evento evento = new Evento();

        // Act
        Evento result = adapter.creacion(evento, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPulep()).isEqualTo("PULEP-001");
        assertThat(result.getArtistas()).isEqualTo("Los Tigres del Norte");
        assertThat(result.getNombre()).isEqualTo("Concierto de Los Tigres");
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Evento existente")
    void testActualizacionEventoExistente() {
        // Arrange
        EventoEvent event = new EventoEvent();
        event.setId(1L);
        event.setPulep("PULEP-002");
        event.setArtistas("Mana");
        event.setNombre("Concierto de Mana");
        event.setEstado(1);

        Evento eventoExistente = new Evento();
        eventoExistente.setId(1L);
        eventoExistente.setPulep("PULEP-001");
        eventoExistente.setArtistas("Artista Original");
        eventoExistente.setNombre("Evento Original");
        eventoExistente.setEstado(0);

        // Act
        Evento result = adapter.creacion(eventoExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPulep()).isEqualTo("PULEP-002");
        assertThat(result.getArtistas()).isEqualTo("Mana");
        assertThat(result.getNombre()).isEqualTo("Concierto de Mana");
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        EventoEvent event = new EventoEvent();
        event.setId(1L);
        event.setPulep(null);
        event.setArtistas(null);
        event.setNombre("Solo Nombre");
        event.setEstado(1);
        event.setOrganizadoresId(null);
        event.setTemporadaId(null);
        event.setTipoId(null);

        Evento evento = new Evento();

        // Act
        Evento result = adapter.creacion(evento, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPulep()).isNull();
        assertThat(result.getArtistas()).isNull();
        assertThat(result.getNombre()).isEqualTo("Solo Nombre");
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        EventoEvent event = new EventoEvent();
        event.setId(1L);
        event.setNombre("Test Evento");
        event.setEstado(1);

        Evento evento = new Evento();

        // Act
        Evento result = adapter.creacion(evento, event);

        // Assert
        assertThat(result).isSameAs(evento);
    }
}
