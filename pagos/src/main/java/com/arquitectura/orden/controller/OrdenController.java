package com.arquitectura.orden.controller;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.controller.CommonController;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.ptp.PlaceToPlayService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.transaccion.service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ordenes")
public class OrdenController extends CommonController<Orden, OrdenService> {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private ConfigSeguroService configSeguroService;

    @Autowired
    private PlaceToPlayService placeToPlayService;

    /** CREACIÓN DE ORDENES **/

    @PostMapping("/crear-no-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNoNumerados(@RequestParam Long pLocalidadId,
                                                   @RequestParam Long pEventoId,
                                                   @RequestParam String pClienteNumeroDocumento,
                                                   @RequestParam Integer pCantidad) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Orden orden = service.crearOrdenNoNumerada(pCantidad, pEventoId,pClienteNumeroDocumento, pLocalidadId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/crear-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNumerados(@RequestBody List<Ticket> pTickets,
                                                 @RequestParam Long pEventoId,
                                                 @RequestParam String pClienteNumeroDocumento) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Orden orden = service.crearOrdenNumerada(pTickets, pEventoId, pClienteNumeroDocumento);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/crear-individual")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenIndividuales(@RequestParam Long pTicketPadreId,
                                                 @RequestParam Integer pCantidad,
                                                 @RequestParam Long pEventoId,
                                                 @RequestParam String pClienteNumeroDocumento) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Orden orden = service.crearOrdenPalcoIndividual(pTicketPadreId, pCantidad, pEventoId, pClienteNumeroDocumento);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /** FIN CREACION DE ORDENES **/

    /**
     * Actualiza el estado de una orden específica.
     *
     * @param ordenId ID de la orden a actualizar
     * @param estado Nuevo estado a asignar
     * @return ResponseEntity con la orden actualizada
     */
    @PutMapping("/estado/{ordenId}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long ordenId, @RequestParam int estado) {
        Orden ordenActualizada = service.actualizarEstado(ordenId, estado);
        return ResponseEntity.ok(ordenActualizada);
    }

    /**
     * Agrega un ticket a una orden específica.
     *
     * @param ordenId ID de la orden
     * @param ticketId del ticket que se agregara a la orden
     * @return ResponseEntity con la orden actualizada
     */
    @PostMapping("/agregar/orden/{ordenId}/ticket/{ticketId}")
    public ResponseEntity<?> agregarTicket(@PathVariable Long ordenId, @PathVariable Long ticketId){
        Orden agregarTicket = service.agregarTicketAOrden(ordenId,ticketId);
        return ResponseEntity.ok(agregarTicket);
    }

    /**
     * Elimina de una orden el ticket especifico.
     *
     * @param pIdOrden ID de la orden
     * @param pIdTicket id del ticket a eliminar de la orden
     * @return ResponseEntity con la orden actualizada
     */
    @DeleteMapping("/eliminar/orden/{pIdOrden}/ticket/{pIdTicket}")
    public ResponseEntity<?> deleteTicketFromOrden(@PathVariable Long pIdOrden, @PathVariable Long pIdTicket) {
            service.deleteTicketFromOrden(pIdOrden,pIdTicket);
            return ResponseEntity.noContent().build();
    }


    /**
     * Verifica la orden con la pasarela de pagos.
     *
     * @param pIdOrden ID de la orden
     */
    @GetMapping("/manejo-orden/{pIdOrden}")
    @Transactional("transactionManager")
    public void manejoDeTransaccionConPtp(@PathVariable Long pIdOrden) {
        Orden orden = service.findById(pIdOrden);
        if(orden == null) {
            return;
        }
        placeToPlayService.manejarTransaccionConPtp(orden);
    }

    /**
     * Trae todos las ordenes por el cliente id
     * @param numeroDocumento El ID del cliente
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    @GetMapping("/ordenes/cliente/{numeroDocumento}")
    public ResponseEntity<?> getOrdenesByClienteId(@PathVariable String numeroDocumento) {
        Map<String, Object> response = new HashMap<>();
        response.put("ordenes",service.getAllOrdenesByClienteNumeroDocumento(numeroDocumento));
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    @GetMapping("/ver/{pId}")
    public ResponseEntity<?> verPorId(@PathVariable Long pId) {
        Map<String, Object> response = new HashMap<>();
        Orden orden = service.findById(pId);

        if (orden == null) {
            return ResponseEntity.notFound().build();
        }

        List<Cliente> clientes = new ArrayList<>();
        List<Long> idsLocalidades= new ArrayList<>();
        Alcancia alcancia = null;
        if(orden==null) {
            return ResponseEntity.notFound().build();
        }

        List<Ticket> tickets = orden.getTickets();

        if(orden.getTipo()==5) {
            OrdenAlcancia oe = (OrdenAlcancia) orden;
            alcancia = oe.getAlcancia();
            tickets = alcancia.getTickets();
        }

        if(!tickets.isEmpty())
        {
            tickets.forEach(t->{
                if(t.getCliente()!=null) {
                    clientes.add(t.getCliente());
                }
                else {
                    clientes.add(null);
                }
                idsLocalidades.add(t.getLocalidad().getId());
            });
        }

        ConfigSeguro configSeguro = configSeguroService.findAll().stream().findFirst().orElse(null);

        if (configSeguro == null) {
            response.put("mensaje", "el seguro no fue configurado");
            return ResponseEntity.ok(response);
        }

        response.put("cliente", orden.getCliente());
        response.put("orden", orden);
        response.put("alcancia", alcancia);

        if(alcancia!=null) {
            response.put("boletasAlcancia",alcancia.getTickets());
        }

        response.put("clientes", clientes);
        response.put("tickets", tickets);
        response.put("transacciones", orden.getTransacciones());

        //Hice esto provisionalmente para que no falle, elimine el feign a eventos
        //ATTE Isaac
        response.put("infoEvento", null);

        response.put("configSeguro", configSeguro);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carrito/{pId}")
    public ResponseEntity<?> getOrdenParaCarrito(@PathVariable Long pId) {

        Map<String, Object> response = new HashMap<>();
        Orden orden = service.findById(pId);

        if (orden == null) {
            return ResponseEntity.notFound().build();
        }
        Evento evento = orden.getEvento();
        List<Ticket> tickets = orden.getTickets();
        Cliente cliente = orden.getCliente();
        ConfigSeguro configSeguro = configSeguroService.getConfigSeguroActivo();

        response.put("evento", evento);
        response.put("cliente", cliente);
        response.put("orden", orden);
        response.put("tickets", tickets);
        response.put("configSeguro", configSeguro);
        response.put("localidad", tickets.get(0).getLocalidad());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orden/respuesta/{pIdOrden}")
    public ResponseEntity<?> getOrdenRespuesta(@PathVariable Long pIdOrden){

        Map<String, Object> response = new HashMap<>();
        Orden orden = service.findById(pIdOrden);
        if (orden == null) {
            return ResponseEntity.notFound().build();
        }
        response.put("orden",orden);
        response.put("transacciones",orden.getTransacciones());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/aplicar-cupon")
    @Transactional("transactionManager")
    public ResponseEntity<?> aplicarCupon(@RequestParam String pCuponId,
                                                   @RequestParam Long pOrdenId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje",service.aplicarCupon(pCuponId, pOrdenId) );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
