package com.arquitectura.ticket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.service.IngresoFactory;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketFactory;

class TicketFactoryTest {

    @InjectMocks
    private TicketFactory ticketFactory;

    @Mock
    private IngresoFactory ingresoFactory;

    private Localidad localidad;
    private List<Dia> dias;
    private List<Ingreso> ingresosMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        localidad = new Localidad();
        localidad.setId(1L);
        localidad.setNombre("Localidad Test");

        Dia dia1 = new Dia();
        Dia dia2 = new Dia();
        dias = Arrays.asList(dia1, dia2);
        localidad.setDias(dias);

        // Mock de ingresos
        Ingreso ingreso1 = new Ingreso();
        Ingreso ingreso2 = new Ingreso();
        ingresosMock = Arrays.asList(ingreso1, ingreso2);
    }

    @Test
    void crear_conParametrosBasicos_creaTicketCorrectamente() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setNumeroTicket("ABC123");
        ticketFactory.setTipo(1);
        ticketFactory.setEstado(0);

        when(ingresoFactory.crearIngresosPorTicket(any(Ticket.class), eq(dias)))
            .thenReturn(ingresosMock);

        Ticket ticket = ticketFactory.crear();

        assertNotNull(ticket);
        assertEquals(localidad, ticket.getLocalidad());
        assertEquals("ABC123", ticket.getNumero());
        assertEquals(1, ticket.getTipo());
        assertEquals(0, ticket.getEstado());
        assertEquals(ingresosMock, ticket.getIngresos());
    }

    @Test
    void crear_sinNumero_creaTicketConNumeroNull() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setTipo(2);
        ticketFactory.setEstado(0);

        when(ingresoFactory.crearIngresosPorTicket(any(Ticket.class), eq(dias)))
            .thenReturn(ingresosMock);

        Ticket ticket = ticketFactory.crear();

        assertNotNull(ticket);
        assertNull(ticket.getNumero());
        assertEquals(2, ticket.getTipo());
        assertEquals(localidad, ticket.getLocalidad());
        assertEquals(ingresosMock, ticket.getIngresos());
    }

    @Test
    void setAsientos_conDosPersonas_creaUnAsiento() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setNumeroTicket("PADRE123");
        ticketFactory.setTipo(1);
        ticketFactory.setEstado(0);

        Ticket ticketPadre = new Ticket();
        ticketPadre.setNumero("PADRE123");

        when(ingresoFactory.crearIngresosPorTicket(any(Ticket.class), eq(dias)))
            .thenReturn(ingresosMock);

        List<Ticket> asientos = ticketFactory.setAsientos(2, ticketPadre);

        assertEquals(1, asientos.size());
        
        Ticket asiento = asientos.get(0);
        assertEquals(localidad, asiento.getLocalidad());
        assertEquals(1, asiento.getTipo());
        assertEquals(0, asiento.getEstado());
        assertEquals(ticketPadre, asiento.getPalco());
        assertEquals(ingresosMock, asiento.getIngresos());
    }

    @Test
    void setAsientos_conCuatroPersonas_creaTresAsientos() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setTipo(1);
        ticketFactory.setEstado(0);

        Ticket ticketPadre = new Ticket();

        when(ingresoFactory.crearIngresosPorTicket(any(Ticket.class), eq(dias)))
            .thenReturn(ingresosMock);

        List<Ticket> asientos = ticketFactory.setAsientos(4, ticketPadre);

        assertEquals(3, asientos.size());
        
        for (Ticket asiento : asientos) {
            assertEquals(localidad, asiento.getLocalidad());
            assertEquals(1, asiento.getTipo());
            assertEquals(0, asiento.getEstado());
            assertEquals(ticketPadre, asiento.getPalco());
            assertEquals(ingresosMock, asiento.getIngresos());
        }
    }

    @Test
    void setAsientos_conUnaPersona_retornaListaVacia() {
        Ticket ticketPadre = new Ticket();

        List<Ticket> asientos = ticketFactory.setAsientos(1, ticketPadre);

        assertEquals(0, asientos.size());
        assertTrue(asientos.isEmpty());
    }

    @Test
    void setAsientos_conCeroPersonas_retornaListaVacia() {
        Ticket ticketPadre = new Ticket();

        List<Ticket> asientos = ticketFactory.setAsientos(0, ticketPadre);

        assertEquals(0, asientos.size());
        assertTrue(asientos.isEmpty());
    }

    @Test
    void gettersYSetters_funcionanCorrectamente() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setNumeroTicket("TEST123");
        ticketFactory.setTipo(5);
        ticketFactory.setEstado(2);

        assertEquals(localidad, ticketFactory.getLocalidad());
        assertEquals("TEST123", ticketFactory.getNumero());
        assertEquals(5, ticketFactory.getTipo());
        assertEquals(2, ticketFactory.getEstado());
    }

    @Test
    void factory_mantieneEstadoEntreCreaciones() {
        ticketFactory.setLocalidad(localidad);
        ticketFactory.setNumeroTicket("TICKET1");
        ticketFactory.setTipo(1);
        ticketFactory.setEstado(0);

        when(ingresoFactory.crearIngresosPorTicket(any(Ticket.class), eq(dias)))
            .thenReturn(ingresosMock);

        Ticket ticket1 = ticketFactory.crear();

        // Cambiar solo el número
        ticketFactory.setNumeroTicket("TICKET2");
        Ticket ticket2 = ticketFactory.crear();

        assertEquals("TICKET1", ticket1.getNumero());
        assertEquals(1, ticket1.getTipo());
        assertEquals(localidad, ticket1.getLocalidad());

        assertEquals("TICKET2", ticket2.getNumero());
        assertEquals(1, ticket2.getTipo());
        assertEquals(localidad, ticket2.getLocalidad());
    }
}