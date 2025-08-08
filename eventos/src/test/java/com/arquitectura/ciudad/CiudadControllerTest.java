package com.arquitectura.ciudad;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.ciudad.controller.CiudadController;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.ciudad.services.CiudadService;
import com.arquitectura.venue.entity.Venue;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CiudadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class CiudadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CiudadService ciudadService;

    @MockBean
    private CiudadRepository repository;

    private Ciudad ciudad;

    @Transactional
    @BeforeEach
    void setup() {
        ciudad = Ciudad.builder()
                .id(1L)
                .nombre("Neiva")
                .venues(null)
                .build();
    }

    @Test
    @DisplayName("Se crea la ciudad de forma exitosa")
    public void createCiudad() throws Exception {
        Ciudad ciudadEsperada = Ciudad.builder()
                .id(1L)
                .nombre("Neiva")
                .build();

        Mockito.when(ciudadService.crear(any(Ciudad.class))).thenReturn(ciudadEsperada);
        mockMvc.perform(post("/ciudades/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Neiva\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ciudad.id").value(1))
                .andExpect(jsonPath("$.ciudad.nombre").value("Neiva"));
    }

    @Test
    @DisplayName("Se modifica la ciudad de forma exitosa")
    public void modifyCiudad() throws Exception {
        Ciudad ciudadModificada = Ciudad.builder()
                .id(1L)
                .nombre("Medellín")
                .venues(new ArrayList<>())
                .build();

        Mockito.when(ciudadService.actualizar(anyLong(), any(Ciudad.class))).thenReturn(ciudadModificada);

        mockMvc.perform(put("/ciudades/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Medellín\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Medellín"));
    }

    @Test
    @DisplayName("Modificar ciudad que no se  encuentra")
    public void actualizarCiudadNoEncontrada() throws Exception {
        Mockito.when(ciudadService.actualizar(anyLong(), any(Ciudad.class))).thenReturn(null);

        mockMvc.perform(put("/ciudades/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Bucaramanga\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(containsString("Ciudad no encontrada")));
    }

    @Test
    @DisplayName("Se borra la ciudad de forma exitosa")
    public void deleteCiudad() throws Exception {
        Ciudad ciudadMock = new Ciudad();
        ciudadMock.setId(1L);
        ciudadMock.setVenues(null);

        Mockito.when(ciudadService.findById(1L)).thenReturn(ciudadMock);
        Mockito.doNothing().when(ciudadService).deleteById(1L);

        mockMvc.perform(delete("/ciudades/ciudad/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(ciudadService, Mockito.times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar ciudad no encontrada")
    public void deleteCiudadNoEncontrada() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Ciudad no encontrada"))
                .when(ciudadService).deleteById(99L);
        mockMvc.perform(delete("/ciudades/ciudad/99"))
                .andExpect(status().isNotFound());
        Mockito.verify(ciudadService, Mockito.times(1)).deleteById(99L);
    }

    @Test
    @DisplayName("Eliminar ciudad con tiene venues asociados")
    public void deleteCiudadConVenues() throws Exception {
        Ciudad ciudadExistente = new Ciudad();
        ciudadExistente.setId(1L);

        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue());
        ciudadExistente.setVenues(venues);
        Mockito.doThrow(new RuntimeException("No puede eliminar una ciudad que contenga venues"))
                .when(ciudadService).deleteById(1L);
        mockMvc.perform(delete("/ciudades/ciudad/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No se pueden eliminar las ciudades que contienen al menos un venue"));
        Mockito.verify(ciudadService, Mockito.times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("listar todas las ciudades")
    public void getAllCiudades() throws Exception {
        Ciudad ciudad = new Ciudad();
        List<Ciudad> ciudades = Arrays.asList(ciudad);
        Mockito.when(ciudadService.findAll()).thenReturn(ciudades);

        mockMvc.perform(get("/ciudades"))
                .andExpect(status().isOk());
    }
}

