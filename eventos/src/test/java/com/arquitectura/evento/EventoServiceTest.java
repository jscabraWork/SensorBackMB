package com.arquitectura.evento;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.temporada.entity.Temporada;

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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class EventoServiceTest {

    @MockBean
    private EventoRepository eventoRepository;

    @Autowired
    private EventoService eventoService;

    private Evento evento1;
    private Evento evento2;
    private Evento evento3;
    private Temporada temporada;

    @BeforeEach
    void setUp() {
        temporada = Temporada.builder()
                .id(10L)
                .nombre("Temporada Prueba")
                .estado(1)
                .build();

        evento1 = Evento.builder()
                .id(1L)
                .nombre("Concierto Rock")
                .artistas("Banda A")
                .pulep("ABC123")
                .estado(2)
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 10, 0,0))
                .temporada(temporada)
                .build();
        evento1.setDias(new ArrayList<>());
        evento2 = Evento.builder()
                .id(2L)
                .nombre("Festival de Jazz")
                .artistas("Banda B")
                .pulep("XYZ789")
                .estado(2)
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 12, 0,0))
                .temporada(temporada)
                .build();

        evento3 = Evento.builder()
                .id(3L)
                .nombre("Evento Oculto")
                .artistas("Artista C")
                .pulep("QWE456")
                .estado(0)
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 17, 0,0))
                .temporada(temporada)
                .build();
    }

    @Test
    void testFindAllByEstadoAndTemporadaId() {
        when(eventoRepository.findAllByEstadoAndTemporadaId(2, 10L))
                .thenReturn(Arrays.asList(evento1, evento2));

        List<Evento> resultado = eventoService.findAllByEstadoAndTemporadaId(2, 10L);

        assertEquals(2, resultado.size());
        verify(eventoRepository, times(1)).findAllByEstadoAndTemporadaId(2, 10L);
    }

    @Test
    void testActualizar() {
        Evento cambios = Evento.builder().nombre("Concierto Actualizado").build();

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento1));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        Evento actualizado = eventoService.actualizar(1L, cambios);

        assertEquals("Concierto Actualizado", actualizado.getNombre());
        verify(eventoRepository).save(evento1);
    }

    @Test
    void testActualizarNoEncontrado() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                eventoService.actualizar(99L, new Evento())
        );

        assertEquals("Evento no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void testActualizarEstado() {
        when(eventoRepository.findById(2L)).thenReturn(Optional.of(evento2));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        Evento actualizado = eventoService.actualizarEstado(2L, 3);

        assertEquals(3, actualizado.getEstado());
        verify(eventoRepository).save(evento2);
    }

    @Test
    void testActualizarEstadoNoEncontrado() {
        when(eventoRepository.findById(88L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                eventoService.actualizarEstado(88L, 1)
        );

        assertEquals("No se encontró un evento con el id proporcionado", exception.getMessage());
    }

    @Test
    void testDeleteByIdExitoso() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento1));
        doNothing().when(eventoRepository).deleteById(1L);

        assertDoesNotThrow(() -> eventoService.deleteById(1L));

        verify(eventoRepository).delete(evento1);
    }

    @Test
    void testDeleteByIdNoEncontrado() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                eventoService.deleteById(99L)
        );

        assertEquals("No se encontró ningún evento con el id proporcionado", exception.getMessage());
        verify(eventoRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteByIdConDiasDebeFallar() {

        Dia dia = Dia.builder()
                .id(1L)
                .nombre("Día 1")
                .build();

        evento1.setDias(List.of(dia));

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento1));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                eventoService.deleteById(1L)
        );

        assertEquals("No se puede eliminar el evento porque tiene días asociados", exception.getMessage());

        verify(eventoRepository, never()).deleteById(any());
    }


    @Test
    @DisplayName("Exitoso - traer evento por id y estados")
    void testEventoPorIdAndEstadoInExitoso() {
        List<Integer> estadosValidos = Arrays.asList(1, 2);

        when(eventoRepository.findByIdAndEstadoIn(1L, estadosValidos))
                .thenReturn(evento1);

        Evento resultado = eventoService.getEventoPorIdAndEstadoIn(1L, estadosValidos);

        assertNotNull(resultado);
        assertEquals(evento1.getId(), resultado.getId());
        assertEquals(evento1.getNombre(), resultado.getNombre());
        assertEquals(evento1.getEstado(), resultado.getEstado());
        verify(eventoRepository, times(1)).findByIdAndEstadoIn(1L, estadosValidos);
    }

    @Test
    @DisplayName("No Exitoso - traer evento por id y estados")
    void testEventoPorIdAndEstadoInNoEncontrado() {
        List<Integer> estadosValidos = Arrays.asList(1, 2);

        when(eventoRepository.findByIdAndEstadoIn(99L, estadosValidos))
                .thenReturn(null);
        Evento resultado = eventoService.getEventoPorIdAndEstadoIn(99L, estadosValidos);

        assertNull(resultado);
        verify(eventoRepository, times(1)).findByIdAndEstadoIn(99L, estadosValidos);
    }


}
