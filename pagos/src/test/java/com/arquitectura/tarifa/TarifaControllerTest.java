package com.arquitectura.tarifa;

import com.arquitectura.PagosApplication;

import com.arquitectura.PagosApplication;
import com.arquitectura.tarifa.controller.TarifaController;
import com.arquitectura.tarifa.service.TarifaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarifaController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarifaService tarifaService;

    @Test
    @DisplayName("Validar si tarifa tiene tickets asociados - Éxito con true")
    public void tieneTicketsAsociados_True() throws Exception {
        Mockito.when(tarifaService.tieneTicketsAsociados(1L)).thenReturn(true);

        mockMvc.perform(get("/tarifas/1/tiene-tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Validar si tarifa tiene tickets asociados - Éxito con false")
    public void tieneTicketsAsociados_False() throws Exception {
        Mockito.when(tarifaService.tieneTicketsAsociados(2L)).thenReturn(false);

        mockMvc.perform(get("/tarifas/2/tiene-tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

}
