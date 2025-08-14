package com.arquitectura.cliente.controller;

import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.dto.MisAlcanciasDto;
import com.arquitectura.dto.MisTicketsDto;
import com.arquitectura.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
public class ClienteController extends CommonControllerString<Cliente, ClienteService> {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private AlcanciaService alcanciaService;

    @GetMapping("/usuario/{pCorreo}")
    public ResponseEntity<?> findByCorreo(@PathVariable String pCorreo) {

        Map<String, Object> response = new HashMap<>();
        response.put("cliente", service.findByCorreo(pCorreo));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/buscar/{pNumeroDocumento}")
    public ResponseEntity<?> findByNumeroDocumento(@PathVariable String pNumeroDocumento) {

        Map<String, Object> response = new HashMap<>();
        response.put("cliente", service.findByNumeroDocumento(pNumeroDocumento));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Obtiene todos los tickets de un cliente organizados por evento
     * Valida que el usuario del token sea el mismo que solicita los tickets o sea administrador
     * @param numeroDocumento Número de documento del cliente
     * @param token Token de autorización
     * @return ResponseEntity con la lista de MisTicketsDto organizados por evento
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @GetMapping("/mis-tickets/{numeroDocumento}")
    public ResponseEntity<?> getMisTicketsByCliente(@PathVariable String numeroDocumento, 
                                                   @RequestHeader("Authorization") String token) {
        try {
            // Validar que el usuario del token sea el mismo o sea admin
            if (!numeroDocumento.equals(service.obtenerUsuarioDeToken(token)) && 
                !service.obtenerRolDeToken(token).equals("ROLE_ADMIN")) {
                return new ResponseEntity<>("No tienes permisos para acceder a estos tickets", HttpStatus.UNAUTHORIZED);
            }
            
            List<MisTicketsDto> misTickets = ticketService.getMisTicketsByCliente(numeroDocumento);
            
            if (misTickets.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(misTickets);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener los tickets del cliente");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todas las alcancías activas de un cliente
     * Valida que el usuario del token sea el mismo que solicita las alcancías o sea administrador
     * @param numeroDocumento Número de documento del cliente
     * @param token Token de autorización
     * @return ResponseEntity con la lista de MisAlcanciasDto activas
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @GetMapping("/mis-alcancias/{numeroDocumento}")
    public ResponseEntity<?> getMisAlcanciasByCliente(@PathVariable String numeroDocumento, 
                                                     @RequestHeader("Authorization") String token) {
        try {
            // Validar que el usuario del token sea el mismo o sea admin
            if (!numeroDocumento.equals(service.obtenerUsuarioDeToken(token)) && 
                !service.obtenerRolDeToken(token).equals("ROLE_ADMIN")) {
                return new ResponseEntity<>("No tienes permisos para acceder a estas alcancías", HttpStatus.UNAUTHORIZED);
            }
            
            List<MisAlcanciasDto> misAlcancias = alcanciaService.getMisAlcanciasByCliente(numeroDocumento);
            
            if (misAlcancias.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(misAlcancias);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener las alcancías del cliente");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
