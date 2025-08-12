package com.arquitectura.ticket.adapter;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para convertir TicketPagos del microservicio de pagos a entidades del microservicio de reportes
 * Implementa el patrón Adapter para manejar las diferencias entre estructuras de datos
 */
@Component
public class TicketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TicketAdapter.class);

    private final TicketService ticketService;

    public TicketAdapter(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    /**
     * Procesa y guarda todos los tickets con cascading automático
     */
    @Transactional("transactionManager")
    public void procesarTicketsPagos(List<TicketPagos> ticketsP, Localidad localidad) {
        logger.info("Procesando {} tickets para localidad {}", ticketsP.size(), localidad.getId());
        
        List<Ticket> tickets = new ArrayList<>();
        
        for (TicketPagos ticketP : ticketsP) {

            //Construir el ticket a partir de TicketPagos
            //Con su localidad, asientos si tiene y ingresos
            Ticket ticket = new Ticket(
                    ticketP.getId(),
                    localidad,
                    ticketP.getTipo(),
                    ticketP.getNumero(),
                    ticketP.getEstado(),
                    ticketP.getIngresosReporte(),
                    ticketP.getAsientosReporte());

            // Establecer relaciones bidireccionales antes de guardar
            //Relacionar ingresos con el ticket
            if (ticket.getIngresos() != null) { // Asegurarse de que los ingresos no sean nulos aunque no deberia ser posible
                ticket.getIngresos().forEach(ingreso -> ingreso.setTicket(ticket));
            }

            if (ticket.getAsientos() != null) { //Si el ticket tiene asientos
                ticket.getAsientos().forEach(asiento -> {
                    asiento.setPalco(ticket);
                    asiento.setLocalidad(localidad); //IMPORTANTE: Asignar localidad a los asientos
                    if (asiento.getIngresos() != null) {
                        asiento.getIngresos().forEach(ingreso -> ingreso.setTicket(asiento));
                    }
                });
            }
            
            tickets.add(ticket);
        }

        ticketService.saveAll(tickets);
        logger.info("Guardados {} tickets", tickets.size());
    }
}