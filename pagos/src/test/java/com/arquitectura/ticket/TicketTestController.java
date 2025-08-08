package com.arquitectura.ticket;

import com.arquitectura.PagosApplication;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.controller.TicketController;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.google.zxing.WriterException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TicketTestController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;


    private Orden orden1;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        orden1 = Orden.builder()
                .id(1L)
                .valorOrden(20.0)
                .estado(3)
                .tipo(0)
                .tickets(new ArrayList<>())
                .cliente(null)
                .transacciones(new ArrayList<>())
                .build();

        ticket1 = Ticket.builder()
                .id(3L)
                .tipo(0)
                .estado(1)
                .servicios(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        ticket2 = Ticket.builder()
                .id(3L)
                .tipo(0)
                .estado(1)
                .servicios(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();
    }


    @Test
    @DisplayName("Obtener todos los tickets por ordenId")
    public void getAllTicketsByOrdenId() throws Exception {
        Long ordenId = 1L;
        Ticket ticket3 = ticket1;
        Ticket ticket4 = ticket2;
        List<Ticket> tickets = Arrays.asList(ticket1, ticket2);

        when(ticketService.getAllByOrdenId(ordenId)).thenReturn(tickets);

        mockMvc.perform(get("/tickets/listarTickets/{ordenId}", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }


    @Test
    @DisplayName("Obtener tickets por localidadId y estado - ID inválido")
    public void getAllTicketsByLocalidadIdAndEstado_InvalidId() throws Exception {
        mockMvc.perform(get("/tickets/listar/estado/abc?pEstado=1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Obtener ticket por pId, localidadId y estado - éxito")
    void getTicketByLocalidadAndEstado_Success() throws Exception {
        Long pId = 5L;
        Long localidadId = 10L;
        int estado = 1;

        Ticket expectedTicket = Ticket.builder()
                .id(7L)
                .tipo(0)
                .estado(1)
                .build();

        when(ticketService.getByLocalidadAndEstado(pId, localidadId, estado)).thenReturn(expectedTicket);

        mockMvc.perform(get("/tickets/buscar/{pId}", pId)
                        .param("localidadId", localidadId.toString())
                        .param("pEstado", String.valueOf(estado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    @DisplayName("Obtener ticket por ID, localidad y estado - no encontrado")
    public void getTicketByLocalidadAndEstado_NotFound() throws Exception {
        Long pId = 99L;
        Long localidadId = 999L;
        int estado = 1;

        when(ticketService.getByLocalidadAndEstado(pId, localidadId, estado)).thenReturn(null);

        mockMvc.perform(get("/tickets/buscar/{pId}", pId)
                        .param("localidadId", String.valueOf(localidadId))
                        .param("pEstado", String.valueOf(estado)))
                .andExpect(jsonPath("$").doesNotExist()); // o validar con un `null` explícito si devuelves ResponseEntity.ok(null)
    }

    @Test
    @DisplayName("Crear tickets numerados sin letra - éxito")
    void crearTicketsNumeradosSinLetra() throws Exception {
        // Configurar mock
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket());

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        // Ejecutar y verificar
        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "10")
                        .param("numeroAbajo", "1")
                        .param("letra", " ")
                        .param("numerado", "true")
                        .param("personas", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tickets creados exitosamente"))
                .andExpect(jsonPath("$.tickets").isArray());
    }

    @Test
    @DisplayName("Crear tickets numerados con letra - éxito")
    void crearTicketsNumeradosConLetra() throws Exception {
        List<Ticket> tickets = List.of(new Ticket());

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "5")
                        .param("numeroAbajo", "1")
                        .param("letra", "A")
                        .param("numerado", "true")
                        .param("personas", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets[0]").exists());
    }

    @Test
    @DisplayName("Crear tickets no numerados - éxito")
    void crearTicketsNoNumerados() throws Exception {
        List<Ticket> tickets = List.of(new Ticket(), new Ticket());

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "2")
                        .param("numeroAbajo", "1")
                        .param("letra", "")
                        .param("numerado", "false")
                        .param("personas", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets.length()").value(2));
    }

    @Test
    @DisplayName("Crear tickets con múltiples personas (tipo 1 para padre) - éxito")
    void crearTicketsConMultiplesPersonas() throws Exception {
        // Crear ticket padre (tipo 1)
        Ticket ticketPadre = new Ticket();
        ticketPadre.setTipo(1);
        List<Ticket> tickets = List.of(ticketPadre);

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        //  Realiza la petición y verificar solo el tipo del ticket padre
        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "1")
                        .param("numeroAbajo", "1")
                        .param("letra", "")
                        .param("numerado", "false")
                        .param("personas", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets[0].tipo").value(1));

        // Verificación adicional opcional (si necesitas asegurar que se crean los asientos)
        verify(ticketService, Mockito.times(1))
                .crearTickets(any(), eq(1), eq(1), eq(""), eq(false), eq(3));
    }

    @Test
    @DisplayName("Fallo al crear tickets - localidad no encontrada")
    void crearTicketsLocalidadNoEncontrada() throws Exception {

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenThrow(new RuntimeException("Localidad no encontrada"));


        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "999")
                        .param("numeroArriba", "1")
                        .param("numeroAbajo", "1")
                        .param("letra", "")
                        .param("numerado", "false")
                        .param("personas", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Crear tickets con letra 'null' - éxito")
    void crearTicketsConLetraNull() throws Exception {
        List<Ticket> tickets = List.of(new Ticket());

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "1")
                        .param("numeroAbajo", "1")
                        .param("letra", "null") // Prueba específica para letra="null"
                        .param("numerado", "true")
                        .param("personas", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Crear tickets sin letras - éxito")
    void crearTicketsConLetraUndefined() throws Exception {
        List<Ticket> tickets = List.of(new Ticket());

        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(tickets);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "1")
                        .param("numeroAbajo", "1")
                        .param("letra", "undefined") // Prueba específica para letra="undefined"
                        .param("numerado", "true")
                        .param("personas", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Obtener tickets por localidadId y estado - tickets encontrados")
    void getAllTicketsByLocalidadIdAndEstado_Success() throws Exception {
        when(ticketService.getAllByLocalidadIdAndEstado(eq(1L), eq(1), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(ticket1, ticket2)));

        mockMvc.perform(get("/tickets/listar/estado/1")
                        .param("pEstado", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Obtener hijos de un palco")
    void obtenerHijosPalco_Success() throws Exception {
        when(ticketService.obtenerHijosDelPalco(3L)).thenReturn(List.of(ticket1, ticket2));

        mockMvc.perform(get("/tickets/3/hijos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Crear tickets numerados - error interno")
    void crearTicketsNumerados_Error() throws Exception {
        when(ticketService.crearTickets(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean(), anyInt()))
                .thenThrow(new RuntimeException("Error al crear tickets"));

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/crear")
                        .param("localidadId", "1")
                        .param("numeroArriba", "10")
                        .param("numeroAbajo", "1")
                        .param("letra", " ")
                        .param("numerado", "true")
                        .param("personas", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al crear tickets"));
    }

    @Test
    @DisplayName("Eliminar ticket sin órdenes - éxito")
    void eliminarTicket_SinOrdenes() throws Exception {
        Mockito.doNothing().when(ticketService).eliminarSiNoTieneOrdenes(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tickets/borrar/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Eliminar ticket con órdenes - falla lógica")
    void eliminarTicket_ConOrdenes() throws Exception {
        Mockito.doThrow(new IllegalStateException("El ticket tiene órdenes asociadas"))
                .when(ticketService).eliminarSiNoTieneOrdenes(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tickets/borrar/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El ticket tiene órdenes asociadas"));
    }

    @Test
    @DisplayName("Eliminar ticket - error interno")
    void eliminarTicket_ErrorGeneral() throws Exception {
        Mockito.doThrow(new RuntimeException("Error inesperado"))
                .when(ticketService).eliminarSiNoTieneOrdenes(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tickets/borrar/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ocurrió un error al eliminar el ticket."));
    }

    @Test
    @DisplayName("Actualizar estado de ticket exitosamente")
    void updateEstado_Success() throws Exception {
        // Configurar mock
        Map<String, Object> resultadoMap = new HashMap<>();
        resultadoMap.put("id", 1L);
        resultadoMap.put("estado", 2);

        when(ticketService.actualizarEstado(eq(1L), eq(2), eq(false)))
                .thenReturn(resultadoMap);

        // Ejecutar y verificar
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/estado/1")
                        .param("estado", "2")
                        .param("forzar", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value(2));
    }


    @Test
    @DisplayName("Actualizar estado de ticket forzadamente")
    void updateEstado_Forzado() throws Exception {
        // Configurar mock
        Map<String, Object> resultadoMap = new HashMap<>();
        resultadoMap.put("id", 1L);
        resultadoMap.put("estado", 3);

        when(ticketService.actualizarEstado(eq(1L), eq(3), eq(true)))
                .thenReturn(resultadoMap);

        // Ejecutar y verificar
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/estado/1")
                        .param("estado", "3")
                        .param("forzar", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value(3));
    }

    @Test
    @DisplayName("Fallo al actualizar estado - ticket no encontrado")
    void updateEstado_TicketNotFound() throws Exception {
        // Configurar mock para error
        when(ticketService.actualizarEstado(eq(99L), eq(2), eq(false)))
                .thenThrow(new EntityNotFoundException("No se encontró el ticket con ID: 99"));

        // Ejecutar y verificar
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/estado/99")
                        .param("estado", "2")
                        .param("forzar", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el ticket con ID: 99"));
    }

    @Test
    @DisplayName("Envía QR exitosamente - tickets con estado 1")
    void enviarQR_Exitoso() throws Exception {
        Long localidadId = 1L;
        Ticket ticketVendido = Ticket.builder().id(1L).estado(1).build();
        Ticket ticketDisponible = Ticket.builder().id(2L).estado(0).build();

        when(ticketService.findAllByLocalidad(localidadId)).thenReturn(List.of(ticketVendido, ticketDisponible));
        doNothing().when(ticketService).mandarQR(ticketVendido);

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/qrs/{pIdLocalidad}", localidadId))
                .andExpect(status().isOk());

        verify(ticketService).findAllByLocalidad(localidadId);
        verify(ticketService).mandarQR(ticketVendido);
        verify(ticketService, never()).mandarQR(ticketDisponible);
    }

    @Test
    @DisplayName("Envía QR - maneja WriterException correctamente")
    void enviarQR_HandleWriterException() throws Exception {
        Long localidadId = 1L;
        Ticket ticket = Ticket.builder().id(1L).estado(1).build();

        when(ticketService.findAllByLocalidad(localidadId)).thenReturn(List.of(ticket));
        doThrow(new WriterException("Error generando QR")).when(ticketService).mandarQR(ticket);

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/qrs/{pIdLocalidad}", localidadId))
                .andExpect(status().isOk());

        verify(ticketService).mandarQR(ticket);
    }

    @Test
    @DisplayName("Envía QR - maneja IOException correctamente")
    void enviarQR_HandleIOException() throws Exception {
        Long localidadId = 1L;
        Ticket ticket = Ticket.builder().id(1L).estado(1).build();
        when(ticketService.findAllByLocalidad(localidadId)).thenReturn(List.of(ticket));
        doThrow(new IOException("Error de IO")).when(ticketService).mandarQR(ticket);
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/qrs/{pIdLocalidad}", localidadId))
                .andExpect(status().isOk());
        verify(ticketService).mandarQR(ticket);
    }

    @Test
    @DisplayName("Envía QR - sin tickets para enviar")
    void enviarQR_SinTickets() throws Exception {
        Long localidadId = 1L;
        when(ticketService.findAllByLocalidad(localidadId)).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/qrs/{pIdLocalidad}", localidadId))
                .andExpect(status().isOk());
        verify(ticketService, never()).mandarQR(any());
    }

    @Test
    @DisplayName("Envía QR - tickets con estado diferente a 1 no se procesan")
    void enviarQR_SoloTicketsEstado1() throws Exception {
        Long localidadId = 1L;
        List<Ticket> tickets = Arrays.asList(
                Ticket.builder().id(1L).estado(0).build(),
                Ticket.builder().id(2L).estado(2).build(),
                Ticket.builder().id(3L).estado(3).build(),
                Ticket.builder().id(4L).estado(4).build()
        );
        when(ticketService.findAllByLocalidad(localidadId)).thenReturn(tickets);
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/qrs/{pIdLocalidad}", localidadId))
                .andExpect(status().isOk());
        verify(ticketService, never()).mandarQR(any());
    }

    @Test
    @DisplayName("Agregar hijos a palco - éxito")
    void agregarHijos_Success() throws Exception {
        Long ticketPadreId = 1L;
        Integer cantidad = 5;

        doNothing().when(ticketService).agregarHijos(ticketPadreId, cantidad);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/{id}/agregar-hijos", ticketPadreId)
                        .param("cantidad", cantidad.toString()))
                .andExpect(status().isOk());

        verify(ticketService).agregarHijos(ticketPadreId, cantidad);
    }

    @Test
    @DisplayName("Agregar hijos a palco - ticket no encontrado")
    void agregarHijos_TicketNotFound() throws Exception {
        Long ticketPadreId = 999L;
        Integer cantidad = 3;

        doThrow(new EntityNotFoundException("Ticket padre no encontrado"))
                .when(ticketService).agregarHijos(ticketPadreId, cantidad);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/{id}/agregar-hijos", ticketPadreId)
                        .param("cantidad", cantidad.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar hijos a palco - error interno")
    void agregarHijos_InternalError() throws Exception {
        Long ticketPadreId = 1L;
        Integer cantidad = 2;

        doThrow(new RuntimeException("Error al agregar hijos"))
                .when(ticketService).agregarHijos(ticketPadreId, cantidad);

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets/{id}/agregar-hijos", ticketPadreId)
                        .param("cantidad", cantidad.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Actualizar ticket exitosamente")
    void actualizarTicket_Success() throws Exception {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .numero("A5")
                .estado(1)
                .build();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mensaje", "Ticket actualizado exitosamente");
        resultado.put("ticket", ticket);

        when(ticketService.actualizarTicket(any(Ticket.class), eq(false)))
                .thenReturn(resultado);

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"numero\":10,\"estado\":1}")
                        .param("forzar", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ticket actualizado exitosamente"));
    }

    @Test
    @DisplayName("Actualizar ticket con advertencia - conflicto")
    void actualizarTicket_ConAdvertencia() throws Exception {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .numero("A5")
                .estado(1)
                .build();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("advertencia", "El ticket tiene hijos asociados");
        resultado.put("ticket", ticket);

        when(ticketService.actualizarTicket(any(Ticket.class), eq(false)))
                .thenReturn(resultado);

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"numero\":10,\"estado\":1}")
                        .param("forzar", "false"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.advertencia").value("El ticket tiene hijos asociados"));
    }

    @Test
    @DisplayName("Actualizar ticket - ticket no encontrado")
    void actualizarTicket_NotFound() throws Exception {
        when(ticketService.actualizarTicket(any(Ticket.class), eq(false)))
                .thenThrow(new EntityNotFoundException("Ticket no encontrado"));

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"numero\":10,\"estado\":1}")
                        .param("forzar", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Ticket no encontrado"));
    }

    @Test
    @DisplayName("Actualizar ticket - ticket o ID nulos")
    void actualizarTicket_BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":10,\"estado\":1}")
                        .param("forzar", "false"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El ticket o su ID no pueden ser nulos"));
    }

    @Test
    @DisplayName("Agregar cliente a ticket exitosamente")
    void agregarClienteATicket_Success() throws Exception {
        Cliente cliente = Cliente.builder()
                .numeroDocumento("1007")
                .nombre("Juan")
                .correo("juan@example.com")
                .build();

        when(ticketService.agregarTicketACliente(eq(1L), any(Cliente.class), eq("Bearer token123")))
                .thenReturn("Cliente asignado exitosamente al ticket");

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/agregar-cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroDocumento\":1007,\"nombre\":\"Juan\",\"correo\":\"juan@example.com\"}")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Cliente asignado exitosamente al ticket"));
    }

    @Test
    @DisplayName("Agregar cliente a ticket - error en el servicio")
    void agregarClienteATicket_ServiceError() throws Exception {
        when(ticketService.agregarTicketACliente(eq(1L), any(Cliente.class), eq("Bearer token123")))
                .thenThrow(new RuntimeException("Error al asignar cliente"));

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/agregar-cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"email\":\"juan@example.com\"}")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Actualizar estado con advertencia - conflicto HTTP 409")
    void updateEstado_ConflictWarning() throws Exception {
        Map<String, Object> resultadoMap = new HashMap<>();
        resultadoMap.put("id", 1L);
        resultadoMap.put("estado", 2);
        resultadoMap.put("advertencia", "El ticket tiene hijos asociados. Use forzar=true para continuar.");

        when(ticketService.actualizarEstado(eq(1L), eq(2), eq(false)))
                .thenReturn(resultadoMap);

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/estado/1")
                        .param("estado", "2")
                        .param("forzar", "false"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.advertencia").value("El ticket tiene hijos asociados. Use forzar=true para continuar."));
    }

}


