package com.arquitectura.venue;

import static org.assertj.core.api.Assertions.assertThat;

import com.arquitectura.venue.consumer.VenueEventAdapterImpl;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.events.VenueEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VenueEventAdapterTest {
    private VenueEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new VenueEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente VenueEvent a Venue para creación")
    void testCreacionDesdeVenueEvent() {
        VenueEvent event = new VenueEvent();
        event.setId(1L);
        event.setNombre("Estadio El Campín");

        Venue venue = new Venue();
        Venue result = adapter.creacion(venue, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Estadio El Campín");
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Venue existente")
    void testActualizacionVenueExistente() {
        VenueEvent event = new VenueEvent();
        event.setId(1L);
        event.setNombre("Estadio Atanasio Girardot");

        Venue venueExistente = new Venue();
        venueExistente.setId(1L);
        venueExistente.setNombre("Estadio El Campín");

        Venue result = adapter.creacion(venueExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Estadio Atanasio Girardot");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        VenueEvent event = new VenueEvent();
        event.setId(1L);
        event.setNombre(null);

        Venue venue = new Venue();
        Venue result = adapter.creacion(venue, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        VenueEvent event = new VenueEvent();
        event.setId(1L);
        event.setNombre("Coliseo El Pueblo");

        Venue venue = new Venue();
        Venue result = adapter.creacion(venue, event);
        assertThat(result).isSameAs(venue);
    }
}
