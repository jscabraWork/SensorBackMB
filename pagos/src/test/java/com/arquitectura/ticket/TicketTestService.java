package com.arquitectura.ticket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.arquitectura.PagosApplication;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import com.arquitectura.ingreso.service.IngresoFactory;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.ticket.service.TicketFactory;
import com.arquitectura.ticket.service.TicketService;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TicketTestService {

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private LocalidadRepository localidadRepository;

    @MockBean
    private IngresoRepository ingresoRepository;

    @MockBean
    private TicketFactory ticketFactory;

    @MockBean
    private IngresoFactory ingresoFactory;

    @Autowired
    private TicketService ticketService;

    private Ticket ticket1;
    private Ticket ticket2;
    private Localidad localidad;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        localidad = new Localidad();
        localidad.setId(1L);
        localidad.setNombre("Localidad Test");

        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Cliente Test");

        ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setTipo(0);
        ticket1.setEstado(0);
        ticket1.setLocalidad(localidad);
        ticket1.setOrdenes(new ArrayList<>());

        ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setTipo(0);
        ticket2.setEstado(0);
        ticket2.setLocalidad(localidad);
        ticket2.setOrdenes(new ArrayList<>());
    }

    @Test
    void getAllByOrdenId_exitoso() {
        when(ticketRepository.findByOrdenesId(1L)).thenReturn(List.of(ticket1, ticket2));

        List<Ticket> resultado = ticketService.getAllByOrdenId(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ticketRepository).findByOrdenesId(1L);
    }

    @Test
    void getAllByOrdenId_vacio() {
        when(ticketRepository.findByOrdenesId(999L)).thenReturn(Collections.emptyList());

        List<Ticket> resultado = ticketService.getAllByOrdenId(999L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ticketRepository).findByOrdenesId(999L);
    }

    @Test
    void getAllByLocalidadIdAndEstado_exitoso() {
        Page<Ticket> mockPage = mock(Page.class);
        when(ticketRepository.findTicketsCompletosYPalcosPadresByLocalidadAndEstado(anyLong(), anyInt(), any(Pageable.class)))
                .thenReturn(mockPage);
        when(mockPage.getContent()).thenReturn(List.of(ticket1, ticket2));

        Page<Ticket> resultado = ticketService.getAllByLocalidadIdAndEstado(1L, 0, 0, 10);

        assertNotNull(resultado);
        verify(ticketRepository).findTicketsCompletosYPalcosPadresByLocalidadAndEstado(eq(1L), eq(0), any(Pageable.class));
    }

    @Test
    void obtenerHijosDelPalco_exitoso() {
        when(ticketRepository.findByPalcoId(1L)).thenReturn(List.of(ticket1, ticket2));

        List<Ticket> resultado = ticketService.obtenerHijosDelPalco(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ticketRepository).findByPalcoId(1L);
    }

    @Test
    void obtenerHijosDelPalco_vacio() {
        when(ticketRepository.findByPalcoId(999L)).thenReturn(Collections.emptyList());

        List<Ticket> resultado = ticketService.obtenerHijosDelPalco(999L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ticketRepository).findByPalcoId(999L);
    }

    @Test
    void getByLocalidadAndEstado_ticket_normal() {
        when(ticketRepository.findTicketCompletoYPalcoPadreByLocalidadAndEstado(1L, 1L, 0))
                .thenReturn(Optional.of(ticket1));

        Ticket resultado = ticketService.getByLocalidadAndEstado(1L, 1L, 0);

        assertNotNull(resultado);
        assertEquals(1, resultado.getPersonasPorTicket());
        verify(ticketRepository).findTicketCompletoYPalcoPadreByLocalidadAndEstado(1L, 1L, 0);
    }

    @Test
    void getByLocalidadAndEstado_ticket_palco() {
        ticket1.setTipo(1); // Palco padre
        when(ticketRepository.findTicketCompletoYPalcoPadreByLocalidadAndEstado(1L, 1L, 0))
                .thenReturn(Optional.of(ticket1));
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, 4L});
        when(ticketRepository.contarAsientosPorPalco(anyList())).thenReturn(mockResult);

        Ticket resultado = ticketService.getByLocalidadAndEstado(1L, 1L, 0);

        assertNotNull(resultado);
        assertEquals(4, resultado.getPersonasPorTicket());
        verify(ticketRepository).findTicketCompletoYPalcoPadreByLocalidadAndEstado(1L, 1L, 0);
        verify(ticketRepository).contarAsientosPorPalco(anyList());
    }

    @Test
    void getByLocalidadAndEstado_no_encontrado() {
        when(ticketRepository.findTicketCompletoYPalcoPadreByLocalidadAndEstado(99L, 1L, 0))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ticketService.getByLocalidadAndEstado(99L, 1L, 0);
        });

        verify(ticketRepository).findTicketCompletoYPalcoPadreByLocalidadAndEstado(99L, 1L, 0);
    }

    @Test
    void findTicketsByLocalidadIdAndEstado_exitoso() {
        when(ticketRepository.findByLocalidadIdAndEstadoLimitedTo(1L, 0, PageRequest.of(0, 2)))
                .thenReturn(List.of(ticket1, ticket2));

        List<Ticket> resultado = ticketService.findTicketsByLocalidadIdAndEstado(1L, 0, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ticketRepository).findByLocalidadIdAndEstadoLimitedTo(1L, 0, PageRequest.of(0, 2));
    }

    @Test
    void findTicketsByLocalidadIdAndEstado_vacio() {
        when(ticketRepository.findByLocalidadIdAndEstadoLimitedTo(2L, 0, PageRequest.of(0, 5)))
                .thenReturn(Collections.emptyList());

        List<Ticket> resultado = ticketService.findTicketsByLocalidadIdAndEstado(2L, 0, 5);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ticketRepository).findByLocalidadIdAndEstadoLimitedTo(2L, 0, PageRequest.of(0, 5));
    }

    @Test
    void actualizarEstado_exitoso() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.saveAll(anyList())).thenReturn(List.of(ticket1));

        Map<String, Object> resultado = ticketService.actualizarEstado(1L, 2, true);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("ticketsActualizados"));
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    void actualizarEstado_ticket_no_encontrado() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ticketService.actualizarEstado(999L, 2, false);
        });

        verify(ticketRepository).findById(999L);
        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    void actualizarEstado_con_cliente_sin_forzar() {
        ticket1.setCliente(cliente);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));

        Map<String, Object> resultado = ticketService.actualizarEstado(1L, 2, false);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("advertencia"));
        assertTrue(resultado.containsKey("ticketsConCliente"));
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    void eliminarSiNoTieneOrdenes_exitoso() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));

        assertDoesNotThrow(() -> ticketService.eliminarSiNoTieneOrdenes(1L));

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).delete(ticket1);
    }

    @Test
    void eliminarSiNoTieneOrdenes_con_ordenes() {
        ticket1.setOrdenes(List.of(new Orden()));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));

        assertThrows(IllegalStateException.class, () -> {
            ticketService.eliminarSiNoTieneOrdenes(1L);
        });

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void eliminarSiNoTieneOrdenes_no_encontrado() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.eliminarSiNoTieneOrdenes(999L);
        });

        verify(ticketRepository).findById(999L);
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void findAllByLocalidad_exitoso() {
        when(ticketRepository.findByLocalidadId(1L)).thenReturn(List.of(ticket1));

        List<Ticket> resultado = ticketService.findAllByLocalidad(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(ticket1, resultado.get(0));
        verify(ticketRepository).findByLocalidadId(1L);
    }

    @Test
    void findAllByLocalidad_vacio() {
        when(ticketRepository.findByLocalidadId(999L)).thenReturn(Collections.emptyList());

        List<Ticket> resultado = ticketService.findAllByLocalidad(999L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ticketRepository).findByLocalidadId(999L);
    }

    @Test
    void saveAll_exitoso() {
        List<Ticket> tickets = List.of(ticket1, ticket2);
        when(ticketRepository.saveAll(tickets)).thenReturn(tickets);

        List<Ticket> resultado = ticketService.saveAll(tickets);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ticketRepository).saveAll(tickets);
    }

    @Test
    void saveKafka_exitoso() {
        when(ticketRepository.save(ticket1)).thenReturn(ticket1);

        Ticket resultado = ticketService.saveKafka(ticket1);

        assertNotNull(resultado);
        assertEquals(ticket1.getId(), resultado.getId());
        verify(ticketRepository).save(ticket1);
    }

    @Test
    void saveAllKafka_exitoso() {
        List<Ticket> tickets = List.of(ticket1, ticket2);
        when(ticketRepository.saveAll(tickets)).thenReturn(tickets);

        List<Ticket> resultado = ticketService.saveAllKafka(tickets);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ticketRepository).saveAll(tickets);
    }

    @Test
    void deleteById_exitoso() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));

        assertDoesNotThrow(() -> ticketService.deleteById(1L));

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).deleteById(1L);
    }

    @Test
    void deleteById_no_encontrado() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ticketService.deleteById(999L);
        });

        verify(ticketRepository).findById(999L);
        verify(ticketRepository, never()).deleteById(anyLong());
    }
}