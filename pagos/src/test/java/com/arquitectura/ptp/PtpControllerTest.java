package com.arquitectura.ptp;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.arquitectura.ptp.ProcessUrlResponse;
import com.arquitectura.ptp.SessionNotification;
import com.arquitectura.ptp.Status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
public class PtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaceToPlayService placeToPlayService;

    private ProcessUrlResponse processUrlResponse;
    private SessionNotification sessionNotification;

    @BeforeEach
    void setUp() {
        processUrlResponse = new ProcessUrlResponse();
        processUrlResponse.setRequestId(123L);
        processUrlResponse.setProcessUrl("https://test-payment.com");

        sessionNotification = new SessionNotification();
        Status status = new Status();
        status.setStatus("APPROVED");
        status.setDate("2023-01-01T00:00:00-05:00");
        sessionNotification.setRequestId(123L);
        sessionNotification.setReference("1");
        sessionNotification.setStatus(status);
        sessionNotification.setSignature("valid_signature");
    }

    @Test
    void crearLinkPago_exitoso() throws Exception {
        when(placeToPlayService.crearEnlacePago(anyLong(), anyString(), anyBoolean(), any()))
                .thenReturn(processUrlResponse);

        mockMvc.perform(post("/ptp/crear-link")
                        .param("idOrden", "1")
                        .param("url", "https://retorno.com")
                        .param("seguro", "true")
                        .param("aporteAlcancia", "50000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(123L))
                .andExpect(jsonPath("$.processUrl").value("https://test-payment.com"));

        verify(placeToPlayService).crearEnlacePago(1L, "https://retorno.com", true, 50000.0);
    }

    @Test
    void crearLinkPago_sin_aporte_alcancia() throws Exception {
        when(placeToPlayService.crearEnlacePago(anyLong(), anyString(), anyBoolean(), isNull()))
                .thenReturn(processUrlResponse);

        mockMvc.perform(post("/ptp/crear-link")
                        .param("idOrden", "1")
                        .param("url", "https://retorno.com")
                        .param("seguro", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(123L));

        verify(placeToPlayService).crearEnlacePago(1L, "https://retorno.com", false, null);
    }

    @Test
    void crearLinkPago_argumentos_invalidos() throws Exception {
        when(placeToPlayService.crearEnlacePago(anyLong(), anyString(), anyBoolean(), any()))
                .thenThrow(new IllegalArgumentException("Orden no encontrada"));

        mockMvc.perform(post("/ptp/crear-link")
                        .param("idOrden", "999")
                        .param("url", "https://retorno.com")
                        .param("seguro", "false"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Orden no encontrada"));

        verify(placeToPlayService).crearEnlacePago(999L, "https://retorno.com", false, null);
    }

    @Test
    void crearLinkPago_error_interno() throws Exception {
        when(placeToPlayService.crearEnlacePago(anyLong(), anyString(), anyBoolean(), any()))
                .thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(post("/ptp/crear-link")
                        .param("idOrden", "1")
                        .param("url", "https://retorno.com")
                        .param("seguro", "false"))
                .andExpect(status().isInternalServerError());

        verify(placeToPlayService).crearEnlacePago(1L, "https://retorno.com", false, null);
    }

    @Test
    void recibirTransaccionPTP_exitoso() throws Exception {
        when(placeToPlayService.procesarNotificacionPTP(any(SessionNotification.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/ptp/recepcion-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionNotification)))
                .andExpect(status().isOk());

        verify(placeToPlayService).procesarNotificacionPTP(any(SessionNotification.class));
    }

    @Test
    void recibirTransaccionPTP_firma_invalida() throws Exception {
        when(placeToPlayService.procesarNotificacionPTP(any(SessionNotification.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

        mockMvc.perform(post("/ptp/recepcion-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionNotification)))
                .andExpect(status().isForbidden());

        verify(placeToPlayService).procesarNotificacionPTP(any(SessionNotification.class));
    }

    @Test
    void recibirTransaccionPTP_transaccion_repetida() throws Exception {
        when(placeToPlayService.procesarNotificacionPTP(any(SessionNotification.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        mockMvc.perform(post("/ptp/recepcion-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionNotification)))
                .andExpect(status().isConflict());

        verify(placeToPlayService).procesarNotificacionPTP(any(SessionNotification.class));
    }

    @Test
    void recibirTransaccionPTP_error_interno() throws Exception {
        when(placeToPlayService.procesarNotificacionPTP(any(SessionNotification.class)))
                .thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(post("/ptp/recepcion-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionNotification)))
                .andExpect(status().isInternalServerError());

        verify(placeToPlayService).procesarNotificacionPTP(any(SessionNotification.class));
    }
}