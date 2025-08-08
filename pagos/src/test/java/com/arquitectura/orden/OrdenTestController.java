package com.arquitectura.orden;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.controller.OrdenController;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.ptp.*;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;

@WebMvcTest(OrdenController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class OrdenTestController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdenService service;

    @MockBean
    private PlaceToPlayService ptpService;

    @MockBean
    private TransaccionService transaccionService;

    @MockBean
    private ConfigSeguroService configSeguroService;

    @MockBean
    private PtpAdapter ptpAdapter;

    private Orden orden;
    private Ticket ticket;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Cliente Test");

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setEstado(0);
        ticket.setTipo(0);

        orden = new Orden();
        orden.setId(1L);
        orden.setValorOrden(100.0);
        orden.setEstado(3);
        orden.setTipo(1);
        orden.setIdTRXPasarela(1L);
        orden.setCliente(cliente);
        orden.setTickets(new ArrayList<>());
        orden.setTransacciones(new ArrayList<>());
    }

    @Test
    void actualizarEstado_exitoso() throws Exception {
        Long ordenId = 1L;
        int nuevoEstado = 2;
        orden.setEstado(nuevoEstado);

        when(service.actualizarEstado(ordenId, nuevoEstado)).thenReturn(orden);

        mockMvc.perform(put("/ordenes/estado/{ordenId}", ordenId)
                        .param("estado", String.valueOf(nuevoEstado))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordenId))
                .andExpect(jsonPath("$.estado").value(nuevoEstado));

        verify(service).actualizarEstado(ordenId, nuevoEstado);
    }

    @Test
    void agregarTicketAOrden_exitoso() throws Exception {
        Long ordenId = 1L;
        Long ticketId = 1L;
        orden.getTickets().add(ticket);

        when(service.agregarTicketAOrden(ordenId, ticketId)).thenReturn(orden);

        mockMvc.perform(post("/ordenes/agregar/orden/{ordenId}/ticket/{ticketId}", ordenId, ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordenId))
                .andExpect(jsonPath("$.tickets[0].id").value(ticketId));

        verify(service).agregarTicketAOrden(ordenId, ticketId);
    }

    @Test
    void eliminarTicketDeOrden_exitoso() throws Exception {
        Long ordenId = 1L;
        Long ticketId = 1L;

        doNothing().when(service).deleteTicketFromOrden(ordenId, ticketId);

        mockMvc.perform(delete("/ordenes/eliminar/orden/{pIdOrden}/ticket/{pIdTicket}", ordenId, ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteTicketFromOrden(ordenId, ticketId);
    }

    @Test
    void manejoTransaccionAprobada_actualizaOrdenYTickets() throws Exception {
        Long ordenId = 1L;
        Long idTrxPasarela = 1L;

        Ticket ticket1 = new Ticket();
        ticket1.setId(10L);
        ticket1.setEstado(0);

        Ticket ticket2 = new Ticket();
        ticket2.setId(20L);
        ticket2.setEstado(0);

        orden.getTickets().add(ticket1);
        orden.getTickets().add(ticket2);

        Status status = new Status();
        status.setStatus("APPROVED");

        RequestResponse mockResponse = new RequestResponse();
        mockResponse.setStatus(status);
        mockResponse.setRequestId(idTrxPasarela);

        when(service.findById(ordenId)).thenReturn(orden);
        when(ptpService.generarAuth()).thenReturn(new AuthEntity());
        when(ptpService.makePostRequest(anyString(), anyMap(), any()))
                .thenReturn(mockResponse);

        Transaccion transaccionMock = new Transaccion();
        transaccionMock.setStatus(34);
        when(ptpAdapter.crearTransaccion(any(RequestResponse.class)))
                .thenReturn(transaccionMock);

        when(transaccionService.getTransaccionRepetida(anyInt(), anyLong())).thenReturn(null);
        when(transaccionService.save(any(Transaccion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Orden> ordenCaptor = ArgumentCaptor.forClass(Orden.class);
        when(service.save(ordenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/ordenes/manejo-orden/{pIdOrden}", ordenId))
                .andExpect(status().isOk());

        verify(service).findById(ordenId);
        verify(ptpService).generarAuth();
        verify(ptpService).makePostRequest(anyString(), anyMap(), any());
        verify(transaccionService).save(any(Transaccion.class));

        Orden ordenActualizada = ordenCaptor.getValue();
        assertEquals(1, ordenActualizada.getEstado());

        ordenActualizada.getTickets().forEach(t -> {
            assertEquals(1, t.getEstado());
            assertNotNull(t.getCliente());
            assertEquals(cliente.getNumeroDocumento(), t.getCliente().getNumeroDocumento());
        });
    }

    @Test
    void getOrdenesByClienteDocumento_exitoso() throws Exception {
        String documentoValido = "12345678";

        when(service.getAllOrdenesByClienteNumeroDocumento(documentoValido))
                .thenReturn(List.of(orden));

        mockMvc.perform(get("/ordenes/ordenes/cliente/{numeroDocumento}", documentoValido)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).getAllOrdenesByClienteNumeroDocumento(documentoValido);
    }

    @Test
    void verOrdenPorId_exitoso() throws Exception {
        Long ordenId = 1L;
        Long localidadId = 1L;

        Localidad localidad = new Localidad();
        localidad.setId(localidadId);
        
        ticket.setLocalidad(localidad);
        orden.getTickets().add(ticket);

        ConfigSeguro configSeguro = new ConfigSeguro();
        configSeguro.setId(1L);

        when(service.findById(ordenId)).thenReturn(orden);
        when(configSeguroService.findAll()).thenReturn(List.of(configSeguro));

        mockMvc.perform(get("/ordenes/ver/{pId}", ordenId))
                .andExpect(status().isOk());

        verify(service).findById(ordenId);
        verify(configSeguroService).findAll();
    }

    @Test
    void verOrdenPorId_noEncontrada() throws Exception {
        Long ordenId = 1L;

        when(service.findById(ordenId)).thenReturn(null);

        mockMvc.perform(get("/ordenes/ver/{pId}", ordenId))
                .andExpect(status().isNotFound());

        verify(service).findById(ordenId);
    }

    @Test
    void getOrdenParaCarrito_exitoso() throws Exception {
        Long ordenId = 1L;

        Localidad localidad = new Localidad();
        localidad.setId(1L);
        
        ticket.setLocalidad(localidad);
        orden.getTickets().add(ticket);
        
        Evento evento = new Evento();
        evento.setId(1L);
        orden.setEvento(evento);

        ConfigSeguro configSeguro = new ConfigSeguro();
        configSeguro.setId(1L);

        when(service.findById(ordenId)).thenReturn(orden);
        when(configSeguroService.getConfigSeguroActivo()).thenReturn(configSeguro);

        mockMvc.perform(get("/ordenes/carrito/{pId}", ordenId))
                .andExpect(status().isOk());

        verify(service).findById(ordenId);
        verify(configSeguroService).getConfigSeguroActivo();
    }

    @Test
    void getOrdenRespuesta_exitoso() throws Exception {
        Long ordenId = 1L;

        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setStatus(34);
        orden.getTransacciones().add(transaccion);

        when(service.findById(ordenId)).thenReturn(orden);

        mockMvc.perform(get("/ordenes/orden/respuesta/{pIdOrden}", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orden.id").value(ordenId))
                .andExpect(jsonPath("$.transacciones[0].id").value(1L));

        verify(service).findById(ordenId);
    }

    @Test
    void getOrdenRespuesta_noEncontrada() throws Exception {
        Long ordenId = 1L;

        when(service.findById(ordenId)).thenReturn(null);

        mockMvc.perform(get("/ordenes/orden/respuesta/{pIdOrden}", ordenId))
                .andExpect(status().isNotFound());

        verify(service).findById(ordenId);
    }

    @Test
    void crearOrdenNoNumerada_exitoso() throws Exception {
        when(service.crearOrdenNoNumerada(anyInt(), anyLong(), anyString(), anyLong())).thenReturn(orden);

        mockMvc.perform(post("/ordenes/crear-no-numerada")
                        .param("pLocalidadId", "1")
                        .param("pEventoId", "2")
                        .param("pClienteNumeroDocumento", "12345678")
                        .param("pCantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenId").value(1L));

        verify(service).crearOrdenNoNumerada(2, 2L, "12345678", 1L);
    }

    @Test
    void crearOrdenNumerada_exitoso() throws Exception {
        when(service.crearOrdenNumerada(anyList(), anyLong(), anyString())).thenReturn(orden);

        String ticketsJson = "[{\"id\":1,\"tipo\":0}]";

        mockMvc.perform(post("/ordenes/crear-numerada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketsJson)
                        .param("pEventoId", "2")
                        .param("pClienteNumeroDocumento", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenId").value(1L));

        verify(service).crearOrdenNumerada(anyList(), eq(2L), eq("12345678"));
    }

    @Test
    void crearOrdenPalcoIndividual_exitoso() throws Exception {
        when(service.crearOrdenPalcoIndividual(anyLong(), anyInt(), anyLong(), anyString())).thenReturn(orden);

        mockMvc.perform(post("/ordenes/crear-individual")
                        .param("pTicketPadreId", "1")
                        .param("pCantidad", "2")
                        .param("pEventoId", "2")
                        .param("pClienteNumeroDocumento", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenId").value(1L));

        verify(service).crearOrdenPalcoIndividual(1L, 2, 2L, "12345678");
    }
}