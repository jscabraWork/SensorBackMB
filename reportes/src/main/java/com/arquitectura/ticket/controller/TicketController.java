package com.arquitectura.ticket.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.ticket.adapter.TicketPagos;
import com.arquitectura.ticket.adapter.TicketAdapter;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController extends CommonController<Ticket, TicketService> {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private TicketAdapter ticketAdapter;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional("transactionManager")
    @PostMapping("/crear/{pLocalidadId}")
    public ResponseEntity<?> crear(@RequestBody List<TicketPagos> tickets, @PathVariable Long pLocalidadId){

        logger.info("-- INICIO CREACIÓN TICKETS REPORTE --");

        logger.info("LocalidadId: {}, Cantidad tickets recibidos: {}", pLocalidadId, tickets != null ? tickets.size() : 0);
        
        try {
            Map<String, Object> response = new HashMap<>();
            Localidad localidad = localidadService.findById(pLocalidadId);

            if (localidad == null) {
                logger.error("Localidad no encontrada con ID: {}", pLocalidadId);
                response.put("error", "Localidad no encontrada");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            ticketAdapter.procesarTicketsPagos(tickets, localidad);

            response.put("mensaje", "Tickets creados exitosamente");

            logger.info("CREACIÓN TICKETS REPORTE EXITOSA");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error en la creación de tickets reporte: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
