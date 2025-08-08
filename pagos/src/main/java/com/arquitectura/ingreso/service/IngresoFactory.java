package com.arquitectura.ingreso.service;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ticket.entity.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngresoFactory {

    /**
     * Crea un ingreso individual para un ticket y día específico
     */
    public Ingreso crear(Ticket ticket, Dia dia) {

        Ingreso ingreso = new Ingreso (ticket, dia);

        return ingreso;
    }

    public List<Ingreso> crearIngresosPorTicket(Ticket ticket, List<Dia> dias) {
        return dias.stream()
                .map(dia -> crear(ticket, dia))
                .toList();
    }

}