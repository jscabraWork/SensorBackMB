package com.arquitectura.venue;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
import com.arquitectura.venue.services.VenueService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;



@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class VenueServiceTest {

    @MockBean
    private VenueRepository venueRepository;

    @MockBean
    private CiudadRepository ciudadRepository;

    @Autowired
    private VenueService venueService;

    private Venue venue1, venue2, venue3;
    private Ciudad ciudad;

    @BeforeEach
    void setUp() {
        ciudad = Ciudad.builder()
                .id(10L)
                .nombre("Medellín")
                .build();

        venue1 = Venue.builder().id(1L).nombre("Bar Negro").ciudad(ciudad).build();
        venue2 = Venue.builder().id(2L).nombre("El Campin").ciudad(ciudad).build();
        venue3 = Venue.builder().id(3L).nombre("Hollywood").ciudad(ciudad).build();
    }

    // ========== CREATE ==========
    @Test
    @DisplayName("Crear venue exitoso - cuando no existe")
    void crearVenueExitoso() {
        when(venueRepository.findByNombreAndCiudadId("Nuevo Venue", 10L))
                .thenReturn(Optional.empty());
        when(ciudadRepository.findById(10L)).thenReturn(Optional.of(ciudad));
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> {
            Venue v = inv.getArgument(0);
            v.setId(4L);
            return v;
        });

        Venue nuevo = new Venue();
        nuevo.setNombre("Nuevo Venue");

        Venue resultado = venueService.createVenue(10L, nuevo);

        assertThat(resultado.getId()).isEqualTo(4L);
        assertThat(resultado.getCiudad()).isEqualTo(ciudad);
        verify(venueRepository).findByNombreAndCiudadId("Nuevo Venue", 10L);
        verify(venueRepository).save(any(Venue.class));
    }

    @Test
    @DisplayName("Crear venue fallido - cuando ya existe")
    void crearVenueCuandoYaExiste() {
        when(venueRepository.findByNombreAndCiudadId("Hollywood", 10L))
                .thenReturn(Optional.of(venue3));

        Venue duplicado = new Venue();
        duplicado.setNombre("Hollywood");

        assertThatThrownBy(() -> venueService.createVenue(10L, duplicado))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe un venue con el nombre 'Hollywood'");
    }

    @Test
    @DisplayName("Crear venue fallido - ciudad no existe")
    void crearVenueCuandoCiudadNoExiste() {
        when(venueRepository.findByNombreAndCiudadId("Nuevo Venue", 99L))
                .thenReturn(Optional.empty());
        when(ciudadRepository.findById(99L)).thenReturn(Optional.empty());

        Venue nuevo = new Venue();
        nuevo.setNombre("Nuevo Venue");

        assertThatThrownBy(() -> venueService.createVenue(99L, nuevo))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se encontró la ciudad con ID: 99");
    }

    // ========== UPDATE ==========
    @Test
    @DisplayName("Actualizar venue exitoso")
    void actualizarVenueExitoso() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue1));
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> inv.getArgument(0));

        Venue actualizado = new Venue();
        actualizado.setNombre("Bar Negro Actualizado");
        actualizado.setUrlMapa("nuevo-mapa");

        Venue resultado = venueService.updateVenue(actualizado);

        assertThat(resultado.getNombre()).isEqualTo("Bar Negro Actualizado");
        assertThat(resultado.getUrlMapa()).isEqualTo("nuevo-mapa");
        verify(venueRepository).findById(1L);
        verify(venueRepository).save(venue1);
    }

    @Test
    @DisplayName("Actualizar venue fallido - no existe")
    void actualizarVenueNoExiste() {
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.updateVenue(new Venue()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Venue no encontrado con ID: 99");
    }

    @Test
    @DisplayName("Eliminar venue exitoso - sin eventos")
    void eliminarVenueExitoso() {
        venue1.setEventos(null);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue1));
        doNothing().when(venueRepository).deleteById(1L);

        assertDoesNotThrow(() -> venueService.deleteById(1L));
        verify(venueRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar venue fallido - con eventos")
    void eliminarVenueConEventos() {
        venue1.setEventos(List.of(new Evento()));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue1));

        assertThatThrownBy(() -> venueService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puede eliminar un venue que contenga eventos");
    }

    @Test
    @DisplayName("Eliminar venue fallido - no existe")
    void eliminarVenueNoExiste() {
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> venueService.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No se encontró ningun venue con el id proporcionado");
    }

    @Test
    @DisplayName("Buscar venues por ciudad - exitoso")
    void buscarPorCiudadExitoso() {
        when(venueRepository.findByCiudadId(10L))
                .thenReturn(List.of(venue1, venue2));

        List<Venue> resultados = venueService.findAllByCiudadId(10L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).extracting(Venue::getNombre)
                .containsExactlyInAnyOrder("Bar Negro", "El Campin");
        verify(venueRepository).findByCiudadId(10L);
    }

    @Test
    @DisplayName("Buscar venues por ciudad - vacío")
    void buscarPorCiudadVacio() {
        when(venueRepository.findByCiudadId(20L))
                .thenReturn(Collections.emptyList());

        List<Venue> resultados = venueService.findAllByCiudadId(20L);

        assertThat(resultados).isEmpty();
        verify(venueRepository).findByCiudadId(20L);
    }
}
