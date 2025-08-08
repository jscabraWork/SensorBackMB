package com.arquitectura.venue;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.venue.controller.VenueController;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.services.VenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private Venue venue;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        venue = Venue.builder()
                .id(1L)
                .nombre("Cali")
                .urlMapa("http")
                .eventos(null)
                .build();
    }

    @Test
    @DisplayName("Crear venue exitoso")
    public void createVenueSuccess() throws Exception {
        when(venueService.createVenue(anyLong(), any(Venue.class)))
                .thenReturn(venue);

        mockMvc.perform(post("/venues/crear/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venue)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venue.id").value(1))
                .andExpect(jsonPath("$.venue.nombre").value("Cali"));
    }

    @Test
    @DisplayName("Crear venue con nombre duplicado - debe fallar")
    public void createVenueDuplicateName() throws Exception {
        when(venueService.createVenue(anyLong(), any(Venue.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un venue con ese nombre"));

        mockMvc.perform(post("/venues/crear/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venue)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe un venue con ese nombre"));
    }

    @Test
    @DisplayName("Modificar venue exitoso")
    public void updateVenueSuccess() throws Exception {
        Venue updatedVenue = Venue.builder()
                .id(1L)
                .nombre("Cali Actualizado")
                .urlMapa("http-updated")
                .build();

        when(venueService.updateVenue(any(Venue.class)))
                .thenReturn(updatedVenue);

        mockMvc.perform(put("/venues/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVenue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cali Actualizado"));
    }

    @Test
    @DisplayName("Modificar venue no encontrado")
    public void updateVenueNotFound() throws Exception {
        when(venueService.updateVenue(any(Venue.class)))
                .thenReturn(null);

        mockMvc.perform(put("/venues/actualizar/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venue)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el venue con ID: 99"));
    }

    @Test
    @DisplayName("Eliminar venue exitoso")
    public void deleteVenueSuccess() throws Exception {
        doNothing().when(venueService).deleteById(1L);

        mockMvc.perform(delete("/venues/venue/1"))
                .andExpect(status().isNoContent());

        verify(venueService).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar venue con eventos asociados")
    public void deleteVenueWithEvents() throws Exception {
        doThrow(new RuntimeException("No puede eliminar un venue que contenga eventos"))
                .when(venueService).deleteById(1L);

        mockMvc.perform(delete("/venues/venue/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No puede eliminar un venue que contenga eventos"));
    }

    @Test
    @DisplayName("Eliminar venue no encontrado")
    public void deleteVenueNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Venue no encontrado"))
                .when(venueService).deleteById(99L);

        mockMvc.perform(delete("/venues/venue/99"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Listar venues por ciudad")
    public void getVenuesByCity() throws Exception {
        List<Venue> venues = Arrays.asList(
                Venue.builder().id(1L).nombre("Venue 1").build(),
                Venue.builder().id(2L).nombre("Venue 2").build()
        );

        when(venueService.findAllByCiudadId(1L)).thenReturn(venues);

        mockMvc.perform(get("/venues/listarVenues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Venue 1"));
    }

    @Test
    @DisplayName("Listar venues por ciudad - vacío")
    public void getVenuesByCityEmpty() throws Exception {
        when(venueService.findAllByCiudadId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/venues/listarVenues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener todos los venues por ciudadId")
    public void getAllVenuesByCiudadId() throws Exception {
        Long ciudadId = 1L;
        Venue venue1 = Venue.builder().id(1L).nombre("Venue1").urlMapa("http://venue1").build();
        Venue venue2 = Venue.builder().id(2L).nombre("Venue2").urlMapa("http://venue2").build();
        List<Venue> venues = Arrays.asList(venue1, venue2);

        Mockito.when(venueService.findAllByCiudadId(ciudadId)).thenReturn(venues);

        mockMvc.perform(get("/venues/listarVenues/{ciudadId}", ciudadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Venue1"))
                .andExpect(jsonPath("$[1].nombre").value("Venue2"));
    }

}
