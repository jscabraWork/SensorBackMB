package com.arquitectura.localidad;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.localidad.controller.LocalidadController;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.services.LocalidadService;
import com.arquitectura.tarifa.entity.Tarifa;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebMvcTest(LocalidadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class LocalidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalidadService localidadService;

    private Localidad localidad;
    private Dia dia;
    private Tarifa tarifa;

    @BeforeEach
    void setup() {
        // Crear localidad
        localidad = Localidad.builder()
                .id(1L)
                .nombre("Localidad de prueba")
                .tarifas(new ArrayList<>())
                .dias(new ArrayList<>())
                .build();

        // Crear día
        dia = Dia.builder()
                .id(1L)
                .nombre("Día de prueba")
                .estado(0)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 1,0,0))
                .horaInicio("09:00")
                .horaFin("18:00")
                .localidades(Collections.emptyList())
                .evento(null)
                .build();

        // Crear tarifa
        tarifa = new Tarifa();
        tarifa.setId(1L);
        tarifa.setNombre("Tarifa de prueba");
        tarifa.setPrecio(50.0);
        tarifa.setServicio(5.0);
        tarifa.setIva(12.0);
        tarifa.setEstado(0);
        tarifa.setLocalidad(null);
        // Asignar relaciones
        localidad.setDias(Collections.singletonList(dia));
        localidad.setTarifas(Collections.singletonList(tarifa));
    }

    // ------------------- CREAR LOCALIDAD -------------------

    @Test
    @DisplayName("Crear localidad - Éxito")
    public void crearLocalidadExitoso() throws Exception {
        Mockito.when(localidadService.crear(any(Localidad.class), eq(false))).thenReturn(localidad);

        mockMvc.perform(post("/localidades/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Localidad de prueba\",\"estado\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Localidad de prueba"))
                .andExpect(jsonPath("$.estado").value(0));
    }

    @Test
    @DisplayName("Crear localidad forzando creación - Éxito")
    public void crearLocalidadForzandoCreacionExitoso() throws Exception {
        Mockito.when(localidadService.crear(any(Localidad.class), eq(true))).thenReturn(localidad);

        mockMvc.perform(post("/localidades/crear?forzarCreacion=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Localidad de prueba\",\"estado\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Localidad de prueba"));
    }

    @Test
    @DisplayName("Actualizar localidad - No encontrada")
    public void actualizarLocalidadNoEncontrada() throws Exception {
        Mockito.when(localidadService.actualizar(anyLong(), any(Localidad.class), anyBoolean())).thenReturn(null);

        mockMvc.perform(put("/localidades/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Localidad inexistente\",\"estado\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró la localidad con ID: 99")));
    }

    // ------------------- ELIMINAR LOCALIDAD -------------------

    @Test
    @DisplayName("Eliminar localidad - Éxito")
    public void eliminarLocalidadExitoso() throws Exception {
        Mockito.doNothing().when(localidadService).deleteById(anyLong());

        mockMvc.perform(delete("/localidades/borrar/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(localidadService).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar localidad - Error interno del servidor")
    public void eliminarLocalidadConError() throws Exception {
        Mockito.doThrow(new RuntimeException("Error al eliminar la localidad"))
                .when(localidadService).deleteById(anyLong());

        mockMvc.perform(delete("/localidades/borrar/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al eliminar la localidad")));
    }


    @Test
    @DisplayName("Listar localidades por evento ID y estado - ID inválido")
    public void listarLocalidadesPorEventoYEstadoIdInvalido() throws Exception {
        mockMvc.perform(get("/localidades/listar/abc?pEstado=1"))
                .andExpect(status().isBadRequest());
    }

}
