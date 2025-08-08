package com.arquitectura.orden;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.arquitectura.PagosApplication;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.orden.helper.OrdenCreationHelper;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class OrdenTestService {

    @MockBean
    private OrdenRepository ordenRepository;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private TransaccionRepository transaccionRepository;

    @MockBean
    private LocalidadService localidadService;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private EventoService eventoService;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private OrdenCreationHelper creationHelper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrdenService ordenService;

    private Orden orden;
    private Cliente cliente;
    private Evento evento;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Cliente Test");

        evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Evento Test");

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setEstado(0);
        ticket.setTipo(0);

        orden = new Orden();
        orden.setId(1L);
        orden.setValorOrden(100.0);
        orden.setEstado(3);
        orden.setTipo(1);
        orden.setCliente(cliente);
        orden.setEvento(evento);
        orden.setTickets(new ArrayList<>());
        orden.setTransacciones(new ArrayList<>());
    }

    @Test
    void actualizarEstado_orden_exitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.actualizarEstado(1L, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.getEstado());
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(orden);
    }

    @Test
    void actualizarEstado_orden_no_encontrada() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ordenService.actualizarEstado(1L, 2);
        });

        verify(ordenRepository).findById(1L);
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void agregarTicketAOrden_exitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.agregarTicketAOrden(1L, 1L);

        assertNotNull(resultado);
        assertTrue(resultado.getTickets().contains(ticket));
        verify(ordenRepository).findById(1L);
        verify(ticketRepository).findById(1L);
        verify(ordenRepository).save(orden);
    }

    @Test
    void agregarTicketAOrden_ticket_ya_existente() {
        orden.getTickets().add(ticket);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.agregarTicketAOrden(1L, 1L);
        });

        assertEquals("El ticket ya está asociado a esta orden", exception.getMessage());
        verify(ordenRepository).findById(1L);
        verify(ticketRepository).findById(1L);
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void deleteTicketFromOrden_exitoso() {
        ticket.setOrdenes(new ArrayList<>());
        orden.getTickets().add(ticket);
        ticket.getOrdenes().add(orden);

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        assertDoesNotThrow(() -> ordenService.deleteTicketFromOrden(1L, 1L));

        assertFalse(orden.getTickets().contains(ticket));
        assertFalse(ticket.getOrdenes().contains(orden));
        verify(ordenRepository).findById(1L);
        verify(ticketRepository).findById(1L);
        verify(ordenRepository).save(orden);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void deleteTicketFromOrden_orden_no_encontrada() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.deleteTicketFromOrden(1L, 1L);
        });

        assertEquals("No se encontro ninguna orden con el id proporcionado", exception.getMessage());
        verify(ordenRepository).findById(1L);
        verify(ticketRepository, never()).findById(anyLong());
    }

    @Test
    void getAllOrdenesByClienteNumeroDocumento_exitoso() {
        List<Orden> ordenes = List.of(orden);
        when(ordenRepository.findByClienteNumeroDocumento("12345678")).thenReturn(ordenes);

        List<Orden> resultado = ordenService.getAllOrdenesByClienteNumeroDocumento("12345678");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(orden, resultado.get(0));
        verify(ordenRepository).findByClienteNumeroDocumento("12345678");
    }

    @Test
    void getAllOrdenesByClienteNumeroDocumento_vacio() {
        when(ordenRepository.findByClienteNumeroDocumento("99999999")).thenReturn(new ArrayList<>());

        List<Orden> resultado = ordenService.getAllOrdenesByClienteNumeroDocumento("99999999");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ordenRepository).findByClienteNumeroDocumento("99999999");
    }

    @Test
    void crearOrdenNoNumerada_exitoso() throws Exception {
        when(creationHelper.crearOrdenNoNumerada(anyInt(), anyLong(), anyString(), anyLong(), any()))
                .thenReturn(orden);
        when(ticketService.saveAllKafka(anyList())).thenReturn(new ArrayList<>());
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.crearOrdenNoNumerada(2, 1L, "12345678", 1L);

        assertNotNull(resultado);
        assertEquals(orden.getId(), resultado.getId());
        verify(creationHelper).crearOrdenNoNumerada(eq(2), eq(1L), eq("12345678"), eq(1L), any());
        verify(ticketService).saveAllKafka(anyList());
    }

    @Test
    void crearOrdenNumerada_exitoso() throws Exception {
        List<Ticket> tickets = List.of(ticket);
        when(creationHelper.crearOrdenNumerada(anyList(), anyLong(), anyString(), any()))
                .thenReturn(orden);
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.crearOrdenNumerada(tickets, 1L, "12345678");

        assertNotNull(resultado);
        assertEquals(orden.getId(), resultado.getId());
        verify(creationHelper).crearOrdenNumerada(eq(tickets), eq(1L), eq("12345678"), any());
    }

    @Test
    void crearOrdenPalcoIndividual_exitoso() throws Exception {
        when(creationHelper.crearOrdenPalcoIndividual(anyLong(), anyInt(), anyLong(), anyString(), any()))
                .thenReturn(orden);
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.crearOrdenPalcoIndividual(1L, 2, 1L, "12345678");

        assertNotNull(resultado);
        assertEquals(orden.getId(), resultado.getId());
        verify(creationHelper).crearOrdenPalcoIndividual(eq(1L), eq(2), eq(1L), eq("12345678"), any());
    }

    @Test
    void confirmar_exitoso() throws Exception {
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.confirmar(orden);

        assertNotNull(resultado);
        assertEquals(orden.getId(), resultado.getId());
        verify(ordenRepository).save(orden);
    }

    @Test
    void saveKafka_exitoso() {
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        Orden resultado = ordenService.saveKafka(orden);

        assertNotNull(resultado);
        assertEquals(orden.getId(), resultado.getId());
        verify(ordenRepository).save(orden);
    }

    @Test
    void deleteById_exitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertDoesNotThrow(() -> ordenService.deleteById(1L));

        verify(ordenRepository).findById(1L);
        verify(ordenRepository).deleteById(1L);
    }

    @Test
    void deleteById_orden_no_encontrada() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.deleteById(1L);
        });

        assertEquals("No se encontró ninguna orden con el id proporcionado", exception.getMessage());
        verify(ordenRepository).findById(1L);
        verify(ordenRepository, never()).deleteById(anyLong());
    }
}