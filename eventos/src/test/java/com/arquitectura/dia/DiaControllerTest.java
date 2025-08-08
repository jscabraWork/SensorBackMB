package com.arquitectura.dia;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.dia.controller.DiaController;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.services.DiaService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@WebMvcTest(DiaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class DiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiaService diaService;

    private Dia dia;
    private Localidad localidad;
    private Evento evento;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setup() {
        localidad = Localidad.builder()
                .id(1L)
                .nombre("Localidad de prueba")
                .build();

        evento = Evento.builder()
                .id(1L)
                .nombre("Evento de prueba")
                .build();

        dia = Dia.builder()
                .id(1L)
                .nombre("Día de prueba")
                .estado(1)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 1,0,0))
                .horaInicio("09:00")
                .horaFin("18:00")
                .localidades(Collections.emptyList())
                .build();
    }

    // ------------------- LISTAR POR ESTADO -------------------

    @Test
    @DisplayName("Listar días por estado y evento - Éxito")
    public void listarDiasPorEstadoYEventoExitoso() throws Exception {
        List<Dia> dias = Arrays.asList(dia);
        Mockito.when(diaService.findAllByEstadoAndEventoId(1, 1L)).thenReturn(dias);

        mockMvc.perform(get("/dias/listar/estado?pEstado=1&eventoId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Día de prueba"));
    }

    @Test
    @DisplayName("Listar días por estado y evento - Sin resultados")
    public void listarDiasPorEstadoYEventoSinResultados() throws Exception {
        Mockito.when(diaService.findAllByEstadoAndEventoId(2, 1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dias/listar/estado?pEstado=2&eventoId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ------------------- LISTAR POR EVENTO ID -------------------

    @Test
    @DisplayName("Listar días por evento ID - Éxito")
    public void listarDiasPorEventoIdExitoso() throws Exception {
        Dia dia1 = Dia.builder()
                .id(1L)
                .nombre("Día 1")
                .estado(1)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 1,0,0))
                .horaInicio("09:00")
                .horaFin("12:00")
                .evento(evento)
                .build();

        List<Dia> dias = Collections.singletonList(dia1);

        Mockito.when(diaService.findAllByEventoId(1L)).thenReturn(dias);

        mockMvc.perform(get("/dias/listar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Día 1"));
    }

    @Test
    @DisplayName("Listar días por evento ID - Evento sin días (NOT FOUND)")
    public void listarDiasPorEventoIdSinDias() throws Exception {
        Mockito.when(diaService.findAllByEventoId(99L))
                .thenThrow(new EntityNotFoundException("No se encontraron días para el evento con ID: 99"));

        mockMvc.perform(get("/dias/listar/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontraron días para el evento con ID: 99"));
    }

    @Test
    @DisplayName("Listar días por evento ID - ID inválido")
    public void listarDiasPorEventoIdInvalido() throws Exception {
        mockMvc.perform(get("/dias/listar/abc"))
                .andExpect(status().isBadRequest());
    }

    // ------------------- ACTUALIZAR ESTADO -------------------

    @Test
    @DisplayName("Actualizar estado de día - Éxito")
    public void actualizarEstadoExitoso() throws Exception {
        Dia diaInactivo = Dia.builder()
                .id(1L)
                .nombre("Día de prueba")
                .estado(0)
                .fechaInicio(LocalDateTime.of(2024, 6, 1,0,0))
                .fechaFin(LocalDateTime.of(2024, 6, 2,0,0))
                .horaInicio("09:00")
                .horaFin("18:00")
                .build();

        Mockito.when(diaService.actualizarEstado(anyLong(), anyInt())).thenReturn(diaInactivo);

        mockMvc.perform(put("/dias/estado/1?estado=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(0));
    }

    @Test
    @DisplayName("Actualizar estado de día - No encontrado")
    public void actualizarEstadoNoEncontrado() throws Exception {
        Mockito.when(diaService.actualizarEstado(anyLong(), anyInt())).thenReturn(null);

        mockMvc.perform(put("/dias/estado/99?estado=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("Día no encontrado con ID: 99")));
    }

    // ------------------- ACTUALIZAR DÍA -------------------

    @Test
    @DisplayName("Actualizar día - Éxito")
    public void actualizarDiaExitoso() throws Exception {
        Dia diaActualizado = Dia.builder()
                .id(1L)
                .nombre("Día actualizado")
                .estado(0)
                .fechaInicio(LocalDateTime.now())
                .fechaFin(LocalDateTime.now().plusDays(2))
                .horaInicio("10:00")
                .horaFin("19:00")
                .evento(null)
                .build();

        Mockito.when(diaService.actualizar(anyLong(), any(Dia.class))).thenReturn(diaActualizado);

        mockMvc.perform(put("/dias/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Día actualizado\",\"estado\":0,\"fechaInicio\":\"2024-06-02T10:00:00\",\"fechaFin\":\"2024-06-04T19:00:00\",\"horaInicio\":\"10:00\",\"horaFin\":\"19:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Día actualizado"))
                .andExpect(jsonPath("$.horaInicio").value("10:00"));
    }

    @Test
    @DisplayName("Actualizar día - No encontrado")
    public void actualizarDiaNoEncontrado() throws Exception {
        Mockito.when(diaService.actualizar(anyLong(), any(Dia.class))).thenReturn(null);

        mockMvc.perform(put("/dias/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Día inexistente\",\"estado\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("Día no encontrado con ID: 99")));
    }

    // ------------------- ELIMINAR DÍA -------------------

    @Test
    @DisplayName("Eliminar día - Éxito (sin localidades asociadas)")
    public void eliminarDiaExitosoSinLocalidades() throws Exception {

        dia.setLocalidades(Collections.emptyList());
        Mockito.doNothing().when(diaService).deleteById(anyLong());

        mockMvc.perform(delete("/dias/borrar/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(diaService).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar día - Fallo (con localidades asociadas)")
    public void eliminarDiaConLocalidades() throws Exception {
        // Mockeamos la excepción que lanza tu servicio
        Mockito.doThrow(new RuntimeException("No se puede eliminar el día porque tiene localidades asociadas"))
                .when(diaService).deleteById(anyLong());

        mockMvc.perform(delete("/dias/borrar/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("No se puede eliminar el día porque tiene localidades asociadas"));
    }

    @Test
    @DisplayName("Eliminar día - No encontrado")
    public void eliminarDiaNoEncontrado() throws Exception {
        // Mockeamos la excepción que lanza tu servicio
        Mockito.doThrow(new EntityNotFoundException("No se encontró ningún día con el id proporcionado"))
                .when(diaService).deleteById(anyLong());

        mockMvc.perform(delete("/dias/borrar/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró ningún día con el id proporcionado"));
    }
}
