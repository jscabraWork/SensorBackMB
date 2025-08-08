package com.arquitectura.dia;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.dia.services.DiaService;
import com.arquitectura.localidad.entity.Localidad;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class DiaServiceTest {

    @MockBean
    private DiaRepository diaRepository;

    @Autowired
    private DiaService diaService;

    private Dia dia1;
    private Dia dia2;
    private Dia dia3;
    private Localidad localidad;

    @BeforeEach
    void setUp() {
        dia1 = Dia.builder()
                .id(1L)
                .nombre("Día 1")
                .estado(1)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 10,0,0)) // Fecha de mañana
                .horaInicio("09:00")
                .horaFin("18:00")
                .evento(null)
                .build();

        dia2 = Dia.builder()
                .id(2L)
                .nombre("Día 2")
                .estado(1)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 10,0,0))
                .horaInicio("10:00")
                .horaFin("19:00")
                .evento(null)
                .build();

        dia3 = Dia.builder()
                .id(3L)
                .nombre("Día 3")
                .estado(0)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 1,10,0))
                .horaInicio("08:00")
                .horaFin("17:00")
                .evento(null)
                .build();

        localidad = Localidad.builder()
                .id(1L)
                .nombre("Localidad 1")
                .build();
    }

    @Test
    @DisplayName("findAllByEstadoAndEventoId - Debe retornar días filtrados por estado y evento ID")
    void testFindAllByEstadoAndEventoId() {
        int estado = 1;
        Long eventoId = 123L;

        when(diaRepository.findAllByEstadoAndEventoId(estado, eventoId)).thenReturn(Arrays.asList(dia1, dia2));

        List<Dia> result = diaService.findAllByEstadoAndEventoId(estado, eventoId);

        assertEquals(2, result.size());
        assertTrue(result.contains(dia1));
        assertTrue(result.contains(dia2));
        verify(diaRepository, times(1)).findAllByEstadoAndEventoId(estado, eventoId);
    }

    @Test
    @DisplayName("actualizar - Debe actualizar un día existente")
    void testActualizar() {
        Dia datosActualizados = Dia.builder()
                .nombre("Día Actualizado")
                .horaInicio("10:00")
                .horaFin("19:00")
                .build();

        when(diaRepository.findById(1L)).thenReturn(Optional.of(dia1));
        when(diaRepository.save(any(Dia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dia result = diaService.actualizar(1L, datosActualizados);

        assertEquals("Día Actualizado", result.getNombre());
        assertEquals("10:00", result.getHoraInicio());
        verify(diaRepository).save(dia1);
    }

    @Test
    @DisplayName("actualizarEstado - Debe actualizar el estado de un día")
    void testActualizarEstado() {
        when(diaRepository.findById(1L)).thenReturn(Optional.of(dia1));
        when(diaRepository.save(any(Dia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dia result = diaService.actualizarEstado(1L, 0);

        assertEquals(0, result.getEstado());
        verify(diaRepository).save(dia1);
    }

    @Test
    @DisplayName("actualizar - Debe lanzar excepción cuando el día no existe")
    void testActualizarCuandoNoExiste() {
        when(diaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            diaService.actualizar(99L, new Dia());
        });

        assertEquals("Día no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    @DisplayName("actualizarEstado - Debe lanzar excepción cuando el día no existe")
    void testActualizarEstadoCuandoNoExiste() {
        when(diaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diaService.actualizarEstado(99L, 1);
        });

        assertEquals("No se encontró un día con el id proporcionado", exception.getMessage());
    }

    @Test
    @DisplayName("deleteById - Éxito cuando no hay localidades asociadas")
    void testDeleteByIdExitoso() {
        dia1.setLocalidades(Collections.emptyList());
        when(diaRepository.findById(1L)).thenReturn(Optional.of(dia1));
        doNothing().when(diaRepository).deleteById(1L);

        assertDoesNotThrow(() -> diaService.deleteById(1L));

        verify(diaRepository, times(1)).findById(1L);
        verify(diaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando el día no existe")
    void testDeleteByIdNoEncontrada() {
        when(diaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> diaService.deleteById(99L));

        assertEquals("No se encontró ningún día con el id proporcionado", exception.getMessage());

        verify(diaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando hay localidades asociadas")
    void testDeleteByIdConLocalidades() {
        dia1.setLocalidades(Arrays.asList(localidad));
        when(diaRepository.findById(1L)).thenReturn(Optional.of(dia1));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> diaService.deleteById(1L));

        assertEquals("No se puede eliminar el día porque tiene localidades asociadas", exception.getMessage());
        verify(diaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("save - Debe guardar un nuevo día correctamente")
    void testSave() {
        when(diaRepository.save(any(Dia.class))).thenReturn(dia1);

        Dia result = diaService.save(dia1);

        assertNotNull(result);
        assertEquals("Día 1", result.getNombre());
        verify(diaRepository).save(dia1);
    }

    @Test
    @DisplayName("findById - Debe retornar un día existente")
    void testFindById() {
        when(diaRepository.findById(1L)).thenReturn(Optional.of(dia1));

        Optional<Dia> result = Optional.ofNullable(diaService.findById(1L));

        assertTrue(result.isPresent());
        assertEquals("Día 1", result.get().getNombre());
    }

    @Test
    @DisplayName("findById - Debe retornar vacío cuando el día no existe")
    void testFindByIdNotFound() {
        when(diaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Dia> result = Optional.ofNullable(diaService.findById(99L));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAllByEventoId - Debe retornar días cuando el evento tiene días asociados")
    void testFindAllByEventoIdConDias() {
        Long eventoId = 1L;
        List<Dia> diasEsperados = Arrays.asList(dia1, dia2);

        when(diaRepository.findAllByEventoId(eventoId)).thenReturn(diasEsperados);

        List<Dia> resultado = diaService.findAllByEventoId(eventoId);

        assertEquals(2, resultado.size());
        assertTrue(resultado.containsAll(diasEsperados));
        verify(diaRepository, times(1)).findAllByEventoId(eventoId);
    }

    @Test
    @DisplayName("findAllByEventoId - Debe lanzar excepción cuando el evento no tiene días asociados")
    void testFindAllByEventoIdSinDias() {
        Long eventoId = 99L;

        when(diaRepository.findAllByEventoId(eventoId)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            diaService.findAllByEventoId(eventoId);
        });

        assertEquals("No se encontraron días para el evento con ID: " + eventoId, exception.getMessage());
        verify(diaRepository, times(1)).findAllByEventoId(eventoId);
    }

    @Test
    @DisplayName("findAllByEventoId - Debe llamar al repositorio correctamente")
    void testFindAllByEventoIdLlamaAlRepositorio() {
        Long eventoId = 1L;
        when(diaRepository.findAllByEventoId(eventoId)).thenReturn(Arrays.asList(dia1));

        diaService.findAllByEventoId(eventoId);

        verify(diaRepository, times(1)).findAllByEventoId(eventoId);
        verifyNoMoreInteractions(diaRepository);
    }

}
