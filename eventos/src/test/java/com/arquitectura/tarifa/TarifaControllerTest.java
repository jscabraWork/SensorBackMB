package com.arquitectura.tarifa;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.tarifa.controller.TarifaController;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.services.TarifaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@WebMvcTest(TarifaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class TarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarifaService tarifaService;

    private Tarifa tarifa;
    private Localidad localidad;
    private Evento evento;

    @BeforeEach
    void setup() {
        localidad = Localidad.builder()
                .id(1L)
                .nombre("Localidad de prueba")
                .build();

        tarifa = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa de prueba")
                .precio(100.0)
                .servicio(10.0)
                .iva(21.0)
                .estado(1)
                .localidad(localidad)
                .build();
    }

    // ------------------- LISTAR POR ESTADO Y LOCALIDAD ID -------------------

    @Test
    @DisplayName("Listar tarifas por estado y localidad ID - Éxito")
    public void listarTarifasPorEstadoYLocalidadIdExitoso() throws Exception {
        List<Tarifa> tarifas = Arrays.asList(tarifa);
        Mockito.when(tarifaService.findAllByEstadoAndLocalidadId(1, 1L)).thenReturn(tarifas);

        mockMvc.perform(get("/tarifas/listar/estado?pEstado=1&localidadId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Tarifa de prueba"))
                .andExpect(jsonPath("$[0].precio").value(100.0));
    }

    @Test
    @DisplayName("Listar tarifas por estado y localidad ID - Sin resultados")
    public void listarTarifasPorEstadoYLocalidadIdSinResultados() throws Exception {
        Mockito.when(tarifaService.findAllByEstadoAndLocalidadId(2, 1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tarifas/listar/estado?pEstado=2&localidadId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

// ------------------- LISTAR POR LOCALIDAD ID -------------------

    @Test
    @DisplayName("Listar tarifas por localidad ID - Éxito")
    public void listarTarifasPorLocalidadIdExitoso() throws Exception {
        Tarifa tarifa1 = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa 1")
                .precio(100.0)
                .servicio(10.0)
                .iva(21.0)
                .estado(1)
                .build();

        List<Tarifa> tarifas = Collections.singletonList(tarifa1);

        Mockito.when(tarifaService.findAllByEventoId(1L)).thenReturn(tarifas);

        mockMvc.perform(get("/tarifas/listar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Tarifa 1"));
    }

    @Test
    @DisplayName("Listar tarifas por localidad ID - Localidad sin tarifas (NOT FOUND)")
    public void listarTarifasPorLocalidadIdSinTarifas() throws Exception {
        Mockito.when(tarifaService.findAllByEventoId(99L))
                .thenThrow(new EntityNotFoundException("No se encontraron tarifas para la localidad con ID: 99"));

        mockMvc.perform(get("/tarifas/listar/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontraron tarifas para la localidad con ID: 99"));
    }

    @Test
    @DisplayName("Listar tarifas por evento ID - Evento sin tarifas (NOT FOUND)")
    public void listarTarifasPorEventoIdSinTarifas() throws Exception {
        Mockito.when(tarifaService.findAllByEventoId(99L))
                .thenThrow(new EntityNotFoundException("No se encontraron tarifas para el evento con ID: 99"));

        mockMvc.perform(get("/tarifas/listar/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontraron tarifas para el evento con ID: 99"));
    }

    @Test
    @DisplayName("Listar tarifas por evento ID - ID inválido")
    public void listarTarifasPorEventoIdInvalido() throws Exception {
        mockMvc.perform(get("/tarifas/listar/abc"))
                .andExpect(status().isBadRequest());
    }

    // ------------------- ACTUALIZAR ESTADO -------------------

    @Test
    @DisplayName("Actualizar estado de tarifa - Éxito")
    public void actualizarEstadoExitoso() throws Exception {
        Tarifa tarifaInactiva = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa de prueba")
                .precio(100.0)
                .servicio(10.0)
                .iva(21.0)
                .estado(0)
                .build();

        Mockito.when(tarifaService.actualizarEstado(anyLong(), anyInt())).thenReturn(tarifaInactiva);

        mockMvc.perform(put("/tarifas/estado/1?estado=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(0));
    }

    @Test
    @DisplayName("Actualizar estado de tarifa - No encontrada")
    public void actualizarEstadoNoEncontrado() throws Exception {
        Mockito.when(tarifaService.actualizarEstado(anyLong(), anyInt()))
                .thenThrow(new RuntimeException("Tarifa no encontrada con ID: 99"));

        mockMvc.perform(put("/tarifas/estado/99?estado=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("Tarifa no encontrada con ID: 99")));
    }


    // ------------------- ACTUALIZAR TARIFA -------------------

    @Test
    @DisplayName("Actualizar tarifa - Éxito")
    public void actualizarTarifaExitoso() throws Exception {
        Tarifa tarifaActualizada = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa actualizada")
                .precio(120.0)
                .servicio(12.0)
                .iva(21.0)
                .estado(1)
                .build();

        Mockito.when(tarifaService.actualizar(anyLong(), any(Tarifa.class))).thenReturn(tarifaActualizada);

        mockMvc.perform(put("/tarifas/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tarifa actualizada\",\"precio\":120.0,\"servicio\":12.0,\"iva\":21.0,\"estado\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tarifa actualizada"))
                .andExpect(jsonPath("$.precio").value(120.0));
    }

    @Test
    @DisplayName("Actualizar tarifa - No encontrada")
    public void actualizarTarifaNoEncontrada() throws Exception {
        Mockito.when(tarifaService.actualizar(anyLong(), any(Tarifa.class))).thenReturn(null);

        mockMvc.perform(put("/tarifas/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tarifa inexistente\",\"precio\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("Tarifa no encontrada con ID: 99")));
    }

    // ------------------- ELIMINAR TARIFA -------------------

    @Test
    @DisplayName("Eliminar tarifa - Éxito (sin localidades asociadas)")
    public void eliminarTarifaExitosoSinLocalidades() throws Exception {
        tarifa.setLocalidad(null);
        Mockito.doNothing().when(tarifaService).deleteById(anyLong());

        mockMvc.perform(delete("/tarifas/borrar/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(tarifaService).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar tarifa - Fallo (con localidades asociadas)")
    public void eliminarTarifaConLocalidades() throws Exception {
        Mockito.doThrow(new RuntimeException("No se puede eliminar la tarifa porque tiene localidades asociadas"))
                .when(tarifaService).deleteById(anyLong());

        mockMvc.perform(delete("/tarifas/borrar/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No se puede eliminar la tarifa porque tiene localidades asociadas")));
    }

    @Test
    @DisplayName("Eliminar tarifa - No encontrada")
    public void eliminarTarifaNoEncontrada() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("No se encontró ninguna tarifa con el id proporcionado"))
                .when(tarifaService).deleteById(anyLong());

        mockMvc.perform(delete("/tarifas/borrar/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró ninguna tarifa con el id proporcionado"));

    }
}
