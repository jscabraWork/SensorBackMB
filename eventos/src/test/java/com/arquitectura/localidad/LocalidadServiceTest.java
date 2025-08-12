package com.arquitectura.localidad;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class LocalidadServiceTest {

    @MockBean
    private LocalidadRepository localidadRepository;

    @MockBean
    private TarifaRepository tarifaRepository;

    @MockBean
    private DiaRepository diaRepository;

    @Autowired
    private LocalidadService localidadService;

    private Localidad localidad1;
    private Localidad localidad2;
    private Dia dia;
    private Tarifa tarifa;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba
        dia = Dia.builder()
                .id(1L)
                .nombre("Día de prueba")
                .estado(0)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 1,0,0))
                .horaInicio("09:00")
                .horaFin("18:00")
                .build();

        tarifa = new Tarifa();
        tarifa.setId(1L);
        tarifa.setNombre("Tarifa de prueba");
        tarifa.setPrecio(50.0);

        localidad1 = Localidad.builder()
                .id(1L)
                .nombre("Localidad 1")
                .dias(new ArrayList<>(Collections.singletonList(dia)))
                .tarifas(new ArrayList<>(Collections.singletonList(tarifa)))
                .build();

        localidad2 = Localidad.builder()
                .id(2L)
                .nombre("Localidad 2")
                .dias(new ArrayList<>())
                .tarifas(new ArrayList<>())
                .build();
    }



    @Test
    @DisplayName("actualizarLocalidad - Debe lanzar conflicto cuando el nombre existe y no se fuerza")
    void testActualizarLocalidadConNombreExistente() {
        // 1. Configurar datos de prueba
        String nombreExistente = "Nombre Existente";
        Localidad datosActualizados = Localidad.builder()
                .nombre(nombreExistente)
                .build();

        // 2. Configurar localidad existente con el mismo nombre
        Localidad otraLocalidad = Localidad.builder()
                .id(2L)
                .nombre(nombreExistente)
                .build();

        when(localidadRepository.findById(1L)).thenReturn(Optional.of(localidad1));
        when(localidadRepository.findAllByNombreIgnoreCaseAndIdNot(nombreExistente, 1L))
                .thenReturn(List.of(otraLocalidad));

        // 3. Verificar que lanza la excepción
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localidadService.actualizar(1L, datosActualizados, false);
        });

        // 4. Verificaciones del mensaje de error
        String mensajeError = exception.getMessage();
        assertTrue(mensajeError.contains("Existen una o mas localidades con el nombre: " + nombreExistente),
                "El mensaje de error debería contener: " + nombreExistente);

        // Verificar que NO se intentó guardar
        verify(localidadRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarLocalidad - Debe actualizar una localidad existente forzando actualización")
    void testActualizarLocalidadForzada() {
        Tarifa tarifaReal = new Tarifa();
        tarifaReal.setId(1L);
        tarifaReal.setLocalidad(localidad1);

        Dia diaReal = new Dia();
        diaReal.setId(1L);
        diaReal.setLocalidades(new ArrayList<>());

        localidad1.setTarifas(new ArrayList<>(List.of(tarifaReal)));
        localidad1.setDias(new ArrayList<>(List.of(diaReal)));

        Localidad datosActualizados = Localidad.builder()
                .nombre("Localidad Actualizada Forzada")
                .tarifas(new ArrayList<>())
                .dias(new ArrayList<>())
                .build();

        when(localidadRepository.findById(1L)).thenReturn(Optional.of(localidad1));
        when(localidadRepository.save(any(Localidad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifaReal));
        when(diaRepository.findById(1L)).thenReturn(Optional.of(diaReal));

        Localidad result = localidadService.actualizar(1L, datosActualizados, true);

        assertEquals("Localidad Actualizada Forzada", result.getNombre());
        verify(localidadRepository).save(localidad1);
    }

    @Test
    @DisplayName("actualizarLocalidad - Debe lanzar excepción cuando la localidad no existe")
    void testActualizarLocalidadNoEncontrada() {
        when(localidadRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            localidadService.actualizar(99L, new Localidad(), false);
        });

        assertEquals("Localidad no encontrada con ID: 99", exception.getMessage());
        verify(localidadRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveLocalidad - Debe guardar una nueva localidad sin forzar")
    void testSaveLocalidad() {
        Localidad nuevaLocalidad = Localidad.builder()
                .nombre("Nueva Localidad")
                .tarifas(new ArrayList<>())
                .dias(new ArrayList<>())
                .build();

        // Mockear la verificación de nombre duplicado
        when(localidadRepository.findAllByNombreIgnoreCase("Nueva Localidad")).thenReturn(Collections.emptyList());

        // Mockear el guardado
        when(localidadRepository.save(any(Localidad.class))).thenAnswer(invocation -> {
            Localidad loc = invocation.getArgument(0);
            loc.setId(1L); // Simular ID generado
            return loc;
        });

        Localidad result = localidadService.crear(nuevaLocalidad, false);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Nueva Localidad", result.getNombre());
        assertTrue(result.getDias().isEmpty());

        verify(localidadRepository).findAllByNombreIgnoreCase("Nueva Localidad");
        verify(localidadRepository).save(any(Localidad.class));
    }

    @Test
    @DisplayName("saveLocalidad - Debe guardar una nueva localidad forzando creación")
    void testSaveLocalidadForzada() {
        Localidad nuevaLocalidad = Localidad.builder()
                .nombre("Nueva Localidad Forzada")
                .tarifas(new ArrayList<>())
                .dias(new ArrayList<>())
                .build();

        // Mockear la verificación de nombre para devolver vacío
        when(localidadRepository.findAllByNombreIgnoreCase("Nueva Localidad Forzada"))
                .thenReturn(Collections.emptyList());

        when(localidadRepository.save(any(Localidad.class))).thenAnswer(invocation -> {
            Localidad loc = invocation.getArgument(0);
            loc.setId(2L);
            return loc;
        });

        Localidad result = localidadService.crear(nuevaLocalidad, true);

        assertNotNull(result);
        assertEquals("Nueva Localidad Forzada", result.getNombre());
        verify(localidadRepository).save(any(Localidad.class));
    }

    @Test
    @DisplayName("saveLocalidad - Debe lanzar excepción cuando el nombre existe y no se fuerza")
    void testSaveLocalidadNombreExistente() {
        Localidad existente = Localidad.builder()
                .id(1L)
                .nombre("Localidad Existente")
                .build();

        Localidad nuevaLocalidad = Localidad.builder()
                .nombre("Localidad Existente")
                .build();

        when(localidadRepository.findAllByNombreIgnoreCase("Localidad Existente")).thenReturn(List.of(existente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            localidadService.crear(nuevaLocalidad, false);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Ya existe una localidad con el nombre: Localidad Existente"));

        verify(localidadRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById - Debe eliminar una localidad existente sin relaciones")
    void testDeleteById() {
        localidad1.setTarifas(Collections.emptyList());
        localidad1.setDias(Collections.emptyList());

        when(localidadRepository.findById(1L)).thenReturn(Optional.of(localidad1));
        doNothing().when(localidadRepository).delete(localidad1);

        assertDoesNotThrow(() -> localidadService.deleteById(1L));

        verify(localidadRepository).findById(1L);
        verify(localidadRepository).delete(localidad1);
        verify(localidadRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando la localidad no existe")
    void testDeleteByIdNoEncontrada() {
        when(localidadRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            localidadService.deleteById(99L);
        });

        assertEquals("No se encontró ningún evento con el id proporcionado", exception.getMessage());
        verify(localidadRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando la localidad tiene días y tarifas asociados")
    void testDeleteByIdConDias() {
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(localidad1));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localidadService.deleteById(1L);
        });

        assertEquals("No se puede eliminar la temporada porque tiene dias y tarifas asociados", exception.getMessage());
        verify(localidadRepository, never()).deleteById(any());
    }

    // Tests para los métodos heredados de CommonService

    @Test
    @DisplayName("findAll - Debe retornar todas las localidades")
    void testFindAll() {
        when(localidadRepository.findAll()).thenReturn(Arrays.asList(localidad1, localidad2));

        List<Localidad> result = localidadService.findAll();

        assertEquals(2, result.size());
        verify(localidadRepository).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar una localidad existente")
    void testFindById() {
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(localidad1));

        Localidad result = localidadService.findById(1L);

        assertNotNull(result);
        assertEquals("Localidad 1", result.getNombre());
    }

    @Test
    @DisplayName("findById - Debe retornar null cuando la localidad no existe")
    void testFindByIdNoEncontrada() {
        when(localidadRepository.findById(99L)).thenReturn(Optional.empty());

        Localidad result = localidadService.findById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("save - Debe guardar una localidad")
    void testSave() {
        when(localidadRepository.save(any(Localidad.class))).thenReturn(localidad1);

        Localidad result = localidadService.save(localidad1);

        assertNotNull(result);
        assertEquals("Localidad 1", result.getNombre());
        verify(localidadRepository).save(localidad1);
    }




}
