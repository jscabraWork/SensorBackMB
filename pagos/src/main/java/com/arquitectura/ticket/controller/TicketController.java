package com.arquitectura.ticket.controller;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.controller.CommonController;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.google.zxing.WriterException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController extends CommonController<Ticket, TicketService> {

    /**
     * Obtiene todos los tickets pór la orden id.
     * @return ResponseEntity con la lista de tickets
     */
    @GetMapping("/listarTickets/{ordenId}")
    public ResponseEntity<?> getAllTicketsByOrdenId(@PathVariable Long ordenId) {
        return new ResponseEntity<>(service.getAllByOrdenId(ordenId), HttpStatus.OK);
    }

    /**
     * Obtiene todos los tickets filtrados por localidad y estado.
     *
     * @param localidadId id de la localidad por el cual filtrar los tickets
     * @param pEstado Estado por el cual filtrar los tickets (0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE)
     * @return ResponseEntity con la lista de tickets que coinciden con el estado proporcionado
     */
    @GetMapping("/listar/estado/{localidadId}")
    public ResponseEntity<Page<Ticket>> getAllByLocalidadIdAndEstado(
            @PathVariable Long localidadId,
            @RequestParam int pEstado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Ticket> tickets = service.getAllByLocalidadIdAndEstado(localidadId, pEstado, page, size);

        if (tickets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tickets);
    }

    /**
     * Obtiene todos los tickets hijos de un ticket palco.
     * @param id del ticket de tipo palco
     * @return ResponseEntity con la lista de tickets hijos de un ticket palgo
     */
    @GetMapping("/{id}/hijos")
    public ResponseEntity<List<Ticket>> obtenerHijos(@PathVariable Long id) {
        List<Ticket> hijos = service.obtenerHijosDelPalco(id);
        return ResponseEntity.ok(hijos);
    }

    /**
     * Obtiene un ticket
     * @param pId del ticket a buscar
     * @param localidadId localidad a la que pertenece el ticket a buscar
     * @param pEstado estado en el que se encuentra el ticket
     * @return ResponseEntity con el ticket
     */
    @GetMapping("/buscar/{pId}")
    public ResponseEntity<?> getByLocalidadAndEstado(@PathVariable Long pId,
                                                     @RequestParam Long localidadId,
                                                     @RequestParam int pEstado) {
        Ticket ticket = service.getByLocalidadAndEstado(pId, localidadId, pEstado);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }

    /**
     * Crea múltiples tickets en un rango numérico o no.
     *
     * Este metodo permite la creación de tickets en un rango numérico desde 'numeroAbajo' hasta 'numeroArriba',
     * con opciones para configurar si son numerados, si tienen prefijo de letra, y si cada ticket representa
     * un palco con múltiples asientos.
     *
     * @param localidadId id de la localidad que se le asociara al ticket
     * @param numeroArriba Número más alto en el rango de tickets a crear (inclusivo)
     * @param numeroAbajo Número más bajo en el rango de tickets a crear (inclusivo)
     * @param letra Prefijo opcional para los tickets numerados (puede ser null, "null", "undefined" o espacio en blanco)
     * @param numerado Indica si se debe asignar numeración secuencial a los tickets (true) o no (false)
     * @param personas Cantidad de asientos por ticket; si es mayor a 1, se crearán tickets "esclavos" asociados al ticket principal
     *
     * @return Map con dos posibles estructuras:
     *         - En caso de éxito: {"mensaje": "Tickets creados exitosamente", "tickets": [lista de tickets creados]}
     *         - En caso de error: {"error": "Descripción del error"}
     *
     * @throws RuntimeException Si hay problemas al recuperar la localidad o guardar los tickets
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearNumerado(@RequestParam Long localidadId,
                                            @RequestParam(required = false) Integer numeroArriba,
                                            @RequestParam(required = false) Integer numeroAbajo,
                                            @RequestParam(required = false) String letra,
                                            @RequestParam boolean numerado,
                                            @RequestParam int personas) {
        try {

            List<Ticket> tickets = service.crearTickets(localidadId, numeroArriba, numeroAbajo, letra, numerado, personas);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Tickets creados exitosamente");
            response.put("tickets", tickets);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Agrega hijos a un palco
     * @param pIdTicketPadre id del ticket de tipo palco
     * @param pCantidad cantidad de tickets que se agregaran al palco
     * @return ResponseEntity OK indicando que fue un exito
     */
    @PostMapping("/{id}/agregar-hijos")
    public ResponseEntity<?> agregarHijos(
            @PathVariable("id") Long pIdTicketPadre,
            @RequestParam("cantidad") Integer pCantidad) {

        service.agregarHijos(pIdTicketPadre, pCantidad);
        return ResponseEntity.ok().build();
    }

    /**
     * Actualiza el estado de un ticket o de muchos tickets.
     *
     * @param pId id de ticket a actualizar
     * @param estado Nuevo estado a asignar
     * @return ResponseEntity con el ticket actualizado
     */
    @PutMapping("/estado/{pId}")
    public ResponseEntity<?> updateEstado(
            @PathVariable Long pId,
            @RequestParam int estado,
            @RequestParam(required = false, defaultValue = "false") boolean forzar) {

        try {
            Map<String, Object> resultado = service.actualizarEstado(pId, estado, forzar);

            // Si hay advertencia, significa que hay tickets hijos y no se ha forzado
            if (resultado.containsKey("advertencia")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(resultado);
            }

            return ResponseEntity.ok(resultado);

        } catch (EntityNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", e.getReason());
            return new ResponseEntity<>(errorResponse, e.getStatusCode());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Error al actualizar el estado: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza la información de un ticket o muchos tickets.
     *
     * @param ticket entidad actualizada
     * @param forzar confirmacion de usuario
     * @return ResponseEntity con el ticket actualizado
     */
    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizarTicket(@RequestBody Ticket ticket, @RequestParam boolean forzar) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ticket == null || ticket.getId() == null) {
                response.put("mensaje", "El ticket o su ID no pueden ser nulos");
                return ResponseEntity.badRequest().body(response);
            }
            Map<String, Object> resultado = service.actualizarTicket(ticket, forzar);

            if (resultado.containsKey("advertencia")) {
                return ResponseEntity.status(409).body(resultado);
            }
            return ResponseEntity.ok(resultado);
        } catch (EntityNotFoundException e) {
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al actualizar el ticket: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Asigna un cliente a un ticket.
     *
     * @param cliente cliente al que se le asignara el ticket
     * @param idTicket id del ticket que se asignara
     * @param pToken token para autorizacion de la asignacion
     * @return ResponseEntity 201 con el cliente asignado al ticket
     */
    @PutMapping("/agregar-cliente/{idTicket}")
    public ResponseEntity<?> agregarClienteATicket(@RequestBody Cliente cliente, @PathVariable Long idTicket, @RequestHeader("Authorization")String pToken) throws Exception
    {
        Map<String, Object> response = new HashMap<>();
        String mensaje =service.agregarTicketACliente(idTicket, cliente, pToken);
        response.put("mensaje", mensaje);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * Elimina un ticket por su ID, siempre y cuando no tenga órdenes asociadas.
     *
     * @param pId ID del ticket a eliminar
     * @return ResponseEntity sin contenido si se elimina correctamente,
     *         o con un mensaje de error si el ticket tiene órdenes asociadas
     */
    @DeleteMapping("borrar/{pId}")
    public ResponseEntity<?> eliminarTicket(@PathVariable Long pId) {
        try {
            service.eliminarSiNoTieneOrdenes(pId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocurrió un error al eliminar el ticket.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/qrs/{pIdLocalidad}")
    public ResponseEntity<?> enviarQRS(@PathVariable Long pIdLocalidad){
        Map<String, Object> response = new HashMap<>();
        List<Ticket> tickets = service.findAllByLocalidad(pIdLocalidad);
        tickets.forEach(t->{
            if(t.getEstado()==1) {
                try {
                    service.mandarQR(t);
                } catch (WriterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
