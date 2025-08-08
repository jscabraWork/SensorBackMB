package com.arquitectura.ciudad;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.ciudad.services.CiudadService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.venue.entity.Venue;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class CiudadServiceTest {

    @MockBean
    private CiudadRepository ciudadRepository;

    @Autowired
    private CiudadService ciudadService;

    private Ciudad ciudad1;
    private Ciudad ciudad2;
    private Ciudad ciudad3;

    @BeforeEach
    void setUp() {

        ciudad1 = Ciudad.builder()
                .id(1L)
                .nombre("Neiva")
                .build();


        ciudad2 = Ciudad.builder()
                .id(2L)
                .nombre("Pasto")
                .build();

        ciudad3 = Ciudad.builder()
                .id(3L)
                .nombre("Cucuta")
                .build();


    }

    @Test
    @DisplayName("Crear ciudad cuando no existe - éxito")
    void crearCiudadCuandoNoExiste() {
        when(ciudadRepository.findByNombre("Medellín")).thenReturn(Optional.empty());
        when(ciudadRepository.save(any(Ciudad.class))).thenAnswer(invocation -> {
            Ciudad c = invocation.getArgument(0);
            c.setId(4L);
            return c;
        });

        Ciudad nuevaCiudad = new Ciudad();
        nuevaCiudad.setNombre("Medellín");

        Ciudad resultado = ciudadService.crear(nuevaCiudad);
        verify(ciudadRepository, times(1)).findByNombre("Medellín");
        verify(ciudadRepository, times(1)).save(nuevaCiudad);
    }

    @Test
    @DisplayName("Crear ciudad cuando ya existe - debe lanzar excepción")
    void crearCiudadCuandoYaExiste() {
        when(ciudadRepository.findByNombre("Neiva")).thenReturn(Optional.of(ciudad1));

        Ciudad ciudadDuplicada = new Ciudad();
        ciudadDuplicada.setNombre("Neiva");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> ciudadService.crear(ciudadDuplicada),
                "Debería lanzar ResponseStatusException cuando la ciudad ya existe");
        assertTrue(exception.getReason().contains("Ya existe una ciudad con el nombre: Neiva"),
                "El mensaje de error no coincide");
        verify(ciudadRepository, never()).save(any());
    }

    @Test
    void testActualizar() {
        Ciudad datosActualizados = new Ciudad();
        datosActualizados.setNombre("Ciudad Actualizada");

        when(ciudadRepository.findById(1L)).thenReturn(Optional.of(ciudad1));
        when(ciudadRepository.save(any(Ciudad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ciudad result = ciudadService.actualizar(1L, datosActualizados);

        assertEquals("Ciudad Actualizada", result.getNombre());
        verify(ciudadRepository).save(ciudad1);
    }

    @Test
    void testActualizarCuandoNoExiste() {
        when(ciudadRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            ciudadService.actualizar(99L, new Ciudad());
        });
        assertEquals("Ciudad no encontrada con ID: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Eliminar ciudad exitoso - sin venues")
    void eliminarCiudadExitoso() {
        ciudad1.setVenues(null);
        when(ciudadRepository.findById(1L)).thenReturn(Optional.of(ciudad1));
        doNothing().when(ciudadRepository).deleteById(1L);

        assertDoesNotThrow(() -> ciudadService.deleteById(1L));
        verify(ciudadRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar ciudad fallido - con venues")
    void eliminarCiudadConVenues() {
        ciudad1.setVenues(List.of(new Venue()));
        when(ciudadRepository.findById(1L)).thenReturn(Optional.of(ciudad1));

        assertThatThrownBy(() -> ciudadService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puede eliminar una ciudad que contenga venues");
    }

    @Test
    @DisplayName("Eliminar ciudad fallido - no existe")
    void eliminarCiudadNoExiste() {
        when(ciudadRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ciudadService.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No se encontró ninguna ciudad con el id proporcionado");
    }

}
