package com.arquitectura.alcancia.controller;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.controller.CommonController;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import jdk.jfr.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alcancias")
public class AlcanciaController extends CommonController<Alcancia, AlcanciaService>
{

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{pId}")
    public ResponseEntity<?> getByIdParaAdmin(@PathVariable Long pId) {

        Map<String, Object> response = new HashMap<>();
        Alcancia alcancia = service.findById(pId);

        if (alcancia == null) {
            return ResponseEntity.notFound().build();
        }
        List<Ticket> tickets = alcancia.getTickets();
        Cliente cliente = alcancia.getCliente();
        Localidad localidad = alcancia.getTickets().get(0).getLocalidad();
        Evento evento = localidad.getDias().get(0).getEvento();

        response.put("evento", evento);
        response.put("cliente", cliente);
        response.put("tickets", tickets);
        response.put("localidad", localidad);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/aportar-admin/{pId}")
    public ResponseEntity<?> apotarAlcanciaAdmin(@PathVariable Long pId, @PathVariable Double pValor) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Alcancia alcancia = service.findById(pId);
        if (alcancia == null) {
            return ResponseEntity.notFound().build();
        }
        Alcancia alcanciaBD = service.aportar(alcancia,pValor);
        response.put("alcancia", alcanciaBD);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/agregar-ticket/{pId}")
    public ResponseEntity<?> agregarTicket(@PathVariable Long pId, @PathVariable Long pTicketId) throws Exception {
        Map<String, Object> response = new HashMap<>();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/eliminar-ticket/{pId}")
    public ResponseEntity<?> eliminarTicket(@PathVariable Long pId, @PathVariable Long pTicketId) throws Exception {
        Map<String, Object> response = new HashMap<>();

        return ResponseEntity.ok(response);
    }


}
