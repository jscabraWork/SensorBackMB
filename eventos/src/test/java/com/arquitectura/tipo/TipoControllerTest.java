package com.arquitectura.tipo;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.tipo.controller.TipoController;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.service.TipoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebMvcTest(TipoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class TipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TipoService tipoService;

    private Tipo tipo;

    @Transactional
    @BeforeEach
    void setup() {
        tipo = Tipo.builder()
                .id(1L)
                .nombre("Tipo Prueba")
                .build();
    }

    // ------------------- CREAR TIPO -------------------

    @Test
    @DisplayName("Crear tipo - Éxito")
    public void crearTipoExitoso() throws Exception {
        Mockito.when(tipoService.save(any(Tipo.class))).thenReturn(tipo);

        mockMvc.perform(post("/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tipo Prueba\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tipo Prueba"));
    }

    // ------------------- LISTAR TIPOS -------------------

    @Test
    @DisplayName("Listar todos los tipos - Éxito")
    public void listarTiposExitoso() throws Exception {
        List<Tipo> tipos = Arrays.asList(tipo);
        Mockito.when(tipoService.findAll()).thenReturn(tipos);

        mockMvc.perform(get("/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Tipo Prueba"));
    }

    // ------------------- BUSCAR TIPO POR ID -------------------

    @Test
    @DisplayName("Buscar tipo por ID - Éxito")
    public void buscarTipoPorIdExitoso() throws Exception {
        Mockito.when(tipoService.findById(anyLong())).thenReturn(tipo);

        mockMvc.perform(get("/tipos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tipo Prueba"));
    }

    @Test
    @DisplayName("Buscar tipo por ID - No encontrado")
    public void buscarTipoPorIdNoEncontrado() throws Exception {
        Mockito.when(tipoService.findById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/tipos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar tipo - Éxito")
    public void eliminarTipoExitoso() throws Exception {
        Mockito.when(tipoService.findById(anyLong())).thenReturn(tipo);
        Mockito.doNothing().when(tipoService).deleteById(anyLong());

        mockMvc.perform(delete("/tipos/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(tipoService).deleteById(1L);
    }


}