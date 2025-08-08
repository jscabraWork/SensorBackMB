package com.arquitectura.ingresos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.service.IngresoFactory;
import com.arquitectura.ticket.entity.Ticket;

import java.util.Arrays;
import java.util.List;

class IngresoFactoryTest {

    private IngresoFactory ingresoFactory;

    @BeforeEach
    void setUp() {
        ingresoFactory = new IngresoFactory();
    }

    @Test
    void crear_creaIngresoCorrecto() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        Dia dia = new Dia();
        dia.setId(1L);

        Ingreso ingreso = ingresoFactory.crear(ticket, dia);

        assertNotNull(ingreso);
        assertEquals(ticket, ingreso.getTicket());
        assertEquals(dia, ingreso.getDia());
        assertFalse(ingreso.isUtilizado());
        assertNull(ingreso.getFechaIngreso());
    }

    @Test
    void crear_conTicketNulo_creaIngresoConTicketNulo() {
        Dia dia = new Dia();
        dia.setId(1L);

        Ingreso ingreso = ingresoFactory.crear(null, dia);

        assertNotNull(ingreso);
        assertNull(ingreso.getTicket());
        assertEquals(dia, ingreso.getDia());
        assertFalse(ingreso.isUtilizado());
        assertNull(ingreso.getFechaIngreso());
    }

    @Test
    void crear_conDiaNulo_creaIngresoConDiaNulo() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        Ingreso ingreso = ingresoFactory.crear(ticket, null);

        assertNotNull(ingreso);
        assertEquals(ticket, ingreso.getTicket());
        assertNull(ingreso.getDia());
        assertFalse(ingreso.isUtilizado());
        assertNull(ingreso.getFechaIngreso());
    }

    @Test
    void crearIngresosPorTicket_variosDias_creaListaCorrecta() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        
        Dia dia1 = new Dia();
        dia1.setId(1L);
        Dia dia2 = new Dia();
        dia2.setId(2L);

        List<Dia> dias = Arrays.asList(dia1, dia2);
        List<Ingreso> ingresos = ingresoFactory.crearIngresosPorTicket(ticket, dias);

        assertEquals(2, ingresos.size());

        for (Ingreso ingreso : ingresos) {
            assertEquals(ticket, ingreso.getTicket());
            assertTrue(dias.contains(ingreso.getDia()));
            assertFalse(ingreso.isUtilizado());
            assertNull(ingreso.getFechaIngreso());
        }
    }

    @Test
    void crearIngresosPorTicket_unDia_creaUnIngreso() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        
        Dia dia = new Dia();
        dia.setId(1L);

        List<Dia> dias = Arrays.asList(dia);
        List<Ingreso> ingresos = ingresoFactory.crearIngresosPorTicket(ticket, dias);

        assertEquals(1, ingresos.size());
        assertEquals(ticket, ingresos.get(0).getTicket());
        assertEquals(dia, ingresos.get(0).getDia());
        assertFalse(ingresos.get(0).isUtilizado());
        assertNull(ingresos.get(0).getFechaIngreso());
    }

    @Test
    void crearIngresosPorTicket_listaDiasVacia_retornaListaVacia() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        List<Ingreso> ingresos = ingresoFactory.crearIngresosPorTicket(ticket, Arrays.asList());

        assertNotNull(ingresos);
        assertTrue(ingresos.isEmpty());
    }
}