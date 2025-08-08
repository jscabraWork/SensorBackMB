package com.arquitectura.cliente;

import com.arquitectura.PagosApplication;
import com.arquitectura.cliente.controller.ClienteController;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("Éxito - Cliente encontrado por correo")
    public void testFindByCorreoExitoso() throws Exception {
        String correo = "test@example.com";
        Cliente clienteMock = new Cliente();
        clienteMock.setCorreo(correo);
        clienteMock.setNombre("Test User");

        when(clienteService.findByCorreo(correo)).thenReturn(clienteMock);

        mockMvc.perform(get("/clientes/usuario/{pCorreo}", correo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.correo").value(correo))
                .andExpect(jsonPath("$.cliente.nombre").value("Test User"));

        verify(clienteService, times(1)).findByCorreo(correo);
    }

    @Test
    @DisplayName("Cliente encontrado por correo")
    public void testFindByCorreoNoEncontrado() throws Exception {
        String correo = "noexiste@example.com";

        when(clienteService.findByCorreo(correo)).thenReturn(null);

        mockMvc.perform(get("/clientes/usuario/{pCorreo}", correo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").doesNotExist());

        verify(clienteService, times(1)).findByCorreo(correo);
    }
}
