package com.arquitectura.tarifa;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.tarifa.services.TarifaService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class TarifaServiceTest {

    @MockBean
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaService tarifaService;

    private Tarifa tarifa1;
    private Tarifa tarifa2;
    private Tarifa tarifa3;
    private Localidad localidad;

    @BeforeEach
    void setUp() {
        localidad = Localidad.builder()
                .id(1L)
                .nombre("Localidad 1")
                .build();

        tarifa1 = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa 1")
                .precio(100.0)
                .servicio(10.0)
                .iva(21.0)
                .estado(1)
                .localidad(localidad)
                .build();

        tarifa2 = Tarifa.builder()
                .id(2L)
                .nombre("Tarifa 2")
                .precio(200.0)
                .servicio(20.0)
                .iva(21.0)
                .estado(1)
                .localidad(localidad)
                .build();

        tarifa3 = Tarifa.builder()
                .id(3L)
                .nombre("Tarifa 3")
                .precio(300.0)
                .servicio(30.0)
                .iva(21.0)
                .estado(0)
                .localidad(localidad)
                .build();
;
    }

    @Test
    @DisplayName("findAllByEstadoAndLocalidadId - Debe retornar tarifas filtradas por estado y localidad ID")
    void testFindAllByEstadoAndLocalidadId() {
        int estado = 1;
        Long localidadId = 123L;

        when(tarifaRepository.findAllByEstadoAndLocalidadId(estado, localidadId)).thenReturn(Arrays.asList(tarifa1, tarifa2));

        List<Tarifa> result = tarifaService.findAllByEstadoAndLocalidadId(estado, localidadId);

        assertEquals(2, result.size());
        assertTrue(result.contains(tarifa1));
        assertTrue(result.contains(tarifa2));
        verify(tarifaRepository, times(1)).findAllByEstadoAndLocalidadId(estado, localidadId);
    }

    @Test
    @DisplayName("findAllByEventoId - Debe retornar tarifas cuando el evento tiene tarifas asociadas")
    void testFindAllByEventoIdConTarifas() {
        Long eventoId = 1L;
        List<Tarifa> tarifasEsperadas = Arrays.asList(tarifa1, tarifa2);

        when(tarifaRepository.findAllByEventoId(eventoId)).thenReturn(tarifasEsperadas);

        List<Tarifa> resultado = tarifaService.findAllByEventoId(eventoId);

        assertEquals(2, resultado.size());
        assertTrue(resultado.containsAll(tarifasEsperadas));
        verify(tarifaRepository, times(1)).findAllByEventoId(eventoId);
    }

    @Test
    @DisplayName("findAllByEventoId - Debe lanzar excepción cuando el evento no tiene tarifas asociadas")
    void testFindAllByEventoIdSinTarifas() {
        Long eventoId = 99L;

        when(tarifaRepository.findAllByEventoId(eventoId)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            tarifaService.findAllByEventoId(eventoId);
        });

        assertEquals("No se encontraron tarifas para el evento con ID: " + eventoId, exception.getMessage());
        verify(tarifaRepository, times(1)).findAllByEventoId(eventoId);
    }

    @Test
    @DisplayName("actualizar - Debe actualizar una tarifa existente")
    void testActualizar() {
        Tarifa datosActualizados = Tarifa.builder()
                .nombre("Tarifa Actualizada")
                .precio(150.0)
                .servicio(15.0)
                .iva(21.0)
                .build();

        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa1));
        when(tarifaRepository.save(any(Tarifa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarifa result = tarifaService.actualizar(1L, datosActualizados);

        assertEquals("Tarifa Actualizada", result.getNombre());
        assertEquals(150.0, result.getPrecio());
        assertEquals(15.0, result.getServicio());
        verify(tarifaRepository).save(tarifa1);
    }

    @Test
    @DisplayName("actualizarEstado - Debe actualizar el estado de una tarifa")
    void testActualizarEstado() {
        // Configurar la localidad con una lista vacía de tarifas
        localidad.setTarifas(new ArrayList<>());
        tarifa1.setLocalidad(localidad);

        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa1));
        when(tarifaRepository.save(any(Tarifa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarifa result = tarifaService.actualizarEstado(1L, 0);

        assertEquals(0, result.getEstado());
        verify(tarifaRepository).save(tarifa1);
    }

    @Test
    @DisplayName("actualizar - Debe lanzar excepción cuando la tarifa no existe")
    void testActualizarCuandoNoExiste() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            tarifaService.actualizar(99L, new Tarifa());
        });

        assertEquals("Tarifa no encontrada con ID: 99", exception.getMessage());
    }

    @Test
    @DisplayName("actualizarEstado - Debe lanzar excepción cuando la tarifa no existe")
    void testActualizarEstadoCuandoNoExiste() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarifaService.actualizarEstado(99L, 1);
        });

        assertEquals("No se encontró la tarifa con el id proporcionado", exception.getMessage());
    }

    @Test
    @DisplayName("deleteById - Éxito cuando no hay localidad asociada")
    void testDeleteByIdExitoso() {
        // Crear una nueva tarifa sin localidad para evitar problemas de referencia
        Tarifa tarifaSinLocalidad = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa sin localidad")
                .precio(100.0)
                .servicio(10.0)
                .iva(21.0)
                .estado(1)
                .localidad(null) // Asegurarse que es null
                .build();

        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifaSinLocalidad));
        doNothing().when(tarifaRepository).deleteById(1L);

        assertDoesNotThrow(() -> tarifaService.deleteById(1L));

        verify(tarifaRepository, times(1)).findById(1L);
        verify(tarifaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando la tarifa no existe")
    void testDeleteByIdNoEncontrada() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> tarifaService.deleteById(99L));

        assertEquals("No se encontró ningúna tarifa con el id proporcionado", exception.getMessage());

        verify(tarifaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando hay localidades asociadas")
    void testDeleteByIdConLocalidades() {
        tarifa1.setLocalidad(localidad);
        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa1));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tarifaService.deleteById(1L));

        assertEquals("No se puede eliminar la tarifa porque tiene una localidad asociada", exception.getMessage());
        verify(tarifaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("save - Debe guardar una nueva tarifa correctamente")
    void testSave() {
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(tarifa1);

        Tarifa result = tarifaService.save(tarifa1);

        assertNotNull(result);
        assertEquals("Tarifa 1", result.getNombre());
        verify(tarifaRepository).save(tarifa1);
    }

    @Test
    @DisplayName("findById - Debe retornar una tarifa existente")
    void testFindById() {
        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa1));

        Tarifa result = tarifaService.findById(1L);

        assertNotNull(result);
        assertEquals("Tarifa 1", result.getNombre());
    }

    @Test
    @DisplayName("findById - Debe retornar null cuando la tarifa no existe")
    void testFindByIdNotFound() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        Tarifa result = tarifaService.findById(99L);

        assertNull(result);
    }
}
