package com.arquitectura.qr.controller;

import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qr")
public class QRController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ClienteService clienteService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PutMapping("/enviar/{numeroDocumento}/{pTicketId}")
    public ResponseEntity<?> enviarQR(@PathVariable Long pTicketId, @PathVariable String numeroDocumento,
                                      @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que el usuario del token sea el mismo o sea admin
            if (!numeroDocumento.equals(clienteService.obtenerUsuarioDeToken(token)) &&
                    !clienteService.obtenerRolDeToken(token).equals("ROLE_ADMIN")) {
                return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.UNAUTHORIZED);
            }

            // Obtener el ticket principal evitando cargar relaciones innecesarias
            Ticket ticketPrincipal = ticketService.findById(pTicketId);
            if (ticketPrincipal == null) {
                response.put("error", "Ticket no encontrado");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Validar que el ticket esté vendido
            if (!ticketPrincipal.isVendido()) {
                response.put("error", "El ticket no ha sido vendido");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Ticket> ticketsAEnviar = new ArrayList<>();
            ticketsAEnviar.add(ticketPrincipal);

            // Si es un palco, agregar todos los tickets hijos
            if (ticketPrincipal.getTipo() == 1) {
                List<Ticket> hijos = ticketService.obtenerHijosDelPalco(pTicketId);
                for (Ticket hijo : hijos) {
                    if (!hijo.isVendido()) {
                        response.put("error", "El ticket hijo con ID " + hijo.getId() + " no ha sido vendido");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                    ticketsAEnviar.add(hijo);
                }
            }

            // Enviar QR para todos los tickets
            for (Ticket ticket : ticketsAEnviar) {
                ticketService.mandarQR(ticket);
            }

            response.put("mensaje", "QR(s) enviado(s) correctamente");
            response.put("ticketsEnviados", ticketsAEnviar.size());
            return ResponseEntity.ok(response);

        } catch (WriterException | IOException e) {
            response.put("error", "Error al generar el código QR");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("error", "Error inesperado: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
