package com.arquitectura.evento;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.evento.controller.EventoController;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.services.LocalidadService;
import com.arquitectura.tarifa.services.TarifaService;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.tipo.entity.Tipo;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(EventoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventoService eventoService;

    @MockBean
    private TarifaService tarifaService;

    @MockBean
    private LocalidadService localidadService;

    private Evento evento;
    private SimpleDateFormat dateFormat;
    private Temporada temporada;
    private Tipo tipo;

    @BeforeEach
    void setup() throws Exception {
        temporada = Temporada.builder().id(1L).nombre("Temporada Test").build();
        tipo = Tipo.builder().id(1L).nombre("Concierto").build();

        evento = Evento.builder()
                .id(1L)
                .pulep("EV-2025-001")
                .artistas("Artista Principal, Artista Invitado")
                .nombre("Gran Concierto")
                .recomendaciones("Llegar 1 hora antes")
                .video("https://youtube.com/embed/video123")
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 10, 0)) // Fecha con hora
                .estado(2)
                .temporada(temporada)
                .tipo(tipo)
                .build();
    }

    // ------------------- CREAR EVENTO  -------------------

    @Test
    @DisplayName("Crear evento - Éxito")
    public void crearEventoExitoso() throws Exception {
        Mockito.when(eventoService.actualizar(Mockito.anyLong(), Mockito.any(Evento.class))).thenReturn(null);

        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Gran Concierto\",\"pulep\":\"EV-2025-001\"," +
                                "\"artistas\":\"Artista Principal, Artista Invitado\"," +
                                "\"recomendaciones\":\"Llegar 1 hora antes\"," +
                                "\"video\":\"https://youtube.com/embed/video123\"," +
                                "\"fechaApertura\":\"2025-06-01T10:00\"," +
                                "\"estado\":2," +
                                "\"temporada\":{\"id\":1}," +
                                "\"tipo\":{\"id\":1}}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Crear evento - Falla por datos inválidos")
    public void crearEventoDatosInvalidos() throws Exception {
        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"pulep\":\"\"," +
                                "\"artistas\":\"\"," +
                                "\"fechaApertura\":\"2025-06-01T05:00:00.000Z\"," +
                                "\"estado\":2," +
                                "\"temporada\":{\"id\":1}," +
                                "\"tipo\":{\"id\":1}}"))
                .andExpect(status().isBadRequest());
    }

    // ------------------- LISTAR POR ESTADO Y TEMPORADA -------------------

    @Test
    @DisplayName("Listar eventos por estado y temporada - Éxito")
    public void listarEventosPorEstadoYTemporadaExitoso() throws Exception {
        List<Evento> eventos = Arrays.asList(evento);
        Mockito.when(eventoService.findAllByEstadoAndTemporadaId(anyInt(), anyLong())).thenReturn(eventos);

        mockMvc.perform(get("/eventos/listar/estado?pEstado=2&temporadaId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Gran Concierto"))
                .andExpect(jsonPath("$[0].estado").value(2));
    }

    @Test
    @DisplayName("Listar eventos por estado y temporada - Sin resultados")
    public void listarEventosPorEstadoYTemporadaSinResultados() throws Exception {
        Mockito.when(eventoService.findAllByEstadoAndTemporadaId(anyInt(), anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/eventos/listar/estado?pEstado=1&temporadaId=99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ------------------- ACTUALIZAR ESTADO -------------------

    @Test
    @DisplayName("Actualizar estado de evento - Éxito")
    public void actualizarEstadoExitoso() throws Exception {
        Evento eventoActualizado = Evento.builder()
                .id(1L)
                .nombre("Gran Concierto")
                .estado(1) // OCULTO
                .build();

        Mockito.when(eventoService.actualizarEstado(anyLong(), anyInt())).thenReturn(eventoActualizado);

        mockMvc.perform(put("/eventos/estado/1?estado=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    @DisplayName("Actualizar estado de evento - No encontrado")
    public void actualizarEstadoNoEncontrado() throws Exception {
        Mockito.when(eventoService.actualizarEstado(anyLong(), anyInt())).thenReturn(null);

        mockMvc.perform(put("/eventos/estado/99?estado=1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró el evento con ID: 99")));
    }

    // ------------------- ACTUALIZAR EVENTO -------------------

    @Test
    @DisplayName("Actualizar evento - Éxito")
    public void actualizarEventoExitoso() throws Exception {
        Evento eventoActualizado = Evento.builder()
                .id(1L)
                .pulep("EV-2025-001")
                .artistas("Artista Principal, Artista Invitado, Nuevo Artista")
                .nombre("Gran Concierto Actualizado")
                .recomendaciones("Llegar 2 horas antes")
                .video("https://youtube.com/embed/video456")
                .fechaApertura(LocalDateTime.of(2025, 6, 2, 10, 0)) // Nueva fecha con hora
                .estado(2)
                .temporada(temporada)
                .tipo(tipo)
                .build();

        Mockito.when(eventoService.actualizar(anyLong(), any())).thenReturn(eventoActualizado);

        mockMvc.perform(put("/eventos/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Gran Concierto Actualizado\"," +
                                "\"artistas\":\"Artista Principal, Artista Invitado, Nuevo Artista\"," +
                                "\"recomendaciones\":\"Llegar 2 horas antes\"," +
                                "\"video\":\"https://youtube.com/embed/video456\"," +
                                "\"fechaApertura\":\"2025-06-02T10:00\"," +  // Formato con hora
                                "\"estado\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Gran Concierto Actualizado"))
                .andExpect(jsonPath("$.artistas").value("Artista Principal, Artista Invitado, Nuevo Artista"))
                .andExpect(jsonPath("$.fechaApertura").value("2025-06-02T10:00"));
    }

    @Test
    @DisplayName("Actualizar evento - No encontrado")
    public void actualizarEventoNoEncontrado() throws Exception {
        Mockito.when(eventoService.actualizar(Mockito.anyLong(), Mockito.any(Evento.class))).thenReturn(null);

        mockMvc.perform(put("/eventos/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Evento Inexistente\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró el evento con ID: 99")));
    }

    // ------------------- ELIMINAR EVENTO -------------------

    @Test
    @DisplayName("Eliminar evento - Éxito")
    public void eliminarEventoExitoso() throws Exception {

        Mockito.doNothing().when(eventoService).deleteById(anyLong());

        mockMvc.perform(delete("/eventos/borrar/1"))
                .andExpect(status().isNoContent());

        // Verificar que se llamó al servicio con el ID correcto
        Mockito.verify(eventoService).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar evento - No encontrado")
    public void eliminarEventoNoEncontrado() throws Exception {
        // Configurar el mock para que lance la excepción específica
        Mockito.doThrow(new RuntimeException("No se encontró ningún evento con el id proporcionado"))
                .when(eventoService).deleteById(anyLong());

        mockMvc.perform(delete("/eventos/borrar/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No se encontró ningún evento con el id proporcionado")));
    }

    @Test
    @DisplayName("Obtener evento y localidades para perfil de venta - Éxito")
    public void getEventoParaPerfilVentaExitoso() throws Exception {
        List<Localidad> localidades = Arrays.asList(
                Localidad.builder().id(1L).nombre("Localidad 1").build(),
                Localidad.builder().id(2L).nombre("Localidad 2").build()
        );

        List<Integer> estadosEsperados = Arrays.asList(1, 2);

        Mockito.when(eventoService.getEventoPorIdAndEstadoIn(eq(1L), eq(estadosEsperados))).thenReturn(evento);

        mockMvc.perform(get("/eventos/1/estado"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Obtener evento y localidades para perfil de venta - No exitoso")
    public void getEventoParaPerfilVentaNoEncontrado() throws Exception {
        Long eventId = 99999L;
        List<Integer> estadosEsperados = Arrays.asList(2, 1);

        Mockito.when(eventoService.getEventoPorIdAndEstadoIn(eq(eventId), eq(estadosEsperados))).thenReturn(null);

        mockMvc.perform(get("/eventos/{id}/estado", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evento").doesNotExist());
    }
}