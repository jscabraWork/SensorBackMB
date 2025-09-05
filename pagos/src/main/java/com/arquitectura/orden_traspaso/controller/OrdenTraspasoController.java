package com.arquitectura.orden_traspaso.controller;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.codigo_traspaso.entity.CodigoTraspaso;
import com.arquitectura.codigo_traspaso.service.CodigoTraspasoService;
import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.orden_traspaso.service.OrdenTraspasoService;
import com.arquitectura.ticket.entity.Ticket;
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
@RequestMapping("/ordenes-traspaso")
public class OrdenTraspasoController extends CommonController<OrdenTraspaso, OrdenTraspasoService>{

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CodigoTraspasoService codigoTraspasoService;

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/transferir/{pTicketId}")
    public ResponseEntity<?> transferirTicket(@PathVariable Long pTicketId, @RequestParam String pCorreo, @RequestHeader("Authorization") String pToken) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Ticket ticket = ticketService.findById(pTicketId);

        if(ticket.getEstado()!=1 || ticket.isUtilizado()){
            response.put("mensaje", "Ocurrió un error, no se pudo transferir el ticket");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if(!ticket.getCliente().getNumeroDocumento().equals(clienteService.obtenerUsuarioDeToken(pToken))){
            response.put("mensaje", "Ocurrió un error, no se pudo transferir el ticket");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        codigoTraspasoService.crearCodigoTraspaso(pCorreo, ticket, ticket.getCliente());

        response.put("mensaje", "La persona a quien le cediste el ticket recibirá un correo para confirmar el traspaso");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/confirmar-traspaso/{pCodigo}")
    public ResponseEntity<?> confirmarTransferenciaTicket(@PathVariable String pCodigo,  @RequestHeader("Authorization") String pToken) throws Exception {

        Map<String, Object> response = new HashMap<>();

        CodigoTraspaso codigoTraspaso = codigoTraspasoService.findByCodigo(pCodigo);

        if(codigoTraspaso == null){
            response.put("mensaje", "El codigo que intenta confirmar no existe");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if(!codigoTraspaso.isActivo()){
            response.put("mensaje", "Este código de traspaso ya fue utilizado");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Cliente cliente = codigoTraspaso.getCliente();

        Ticket ticket = codigoTraspaso.getTicket();

        if(!cliente.getNumeroDocumento().equals(ticket.getCliente().getNumeroDocumento())){
            response.put("mensaje", "Ocurrio un error al reclamar el ticket");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Cliente clienteReceptor = clienteService.findById(clienteService.obtenerUsuarioDeToken(pToken));

        if(clienteReceptor == null){
            response.put("mensaje", "Debes estar registrado para reclamar tu ticket");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        service.transferirTicket(ticket, cliente, clienteReceptor);

        List<CodigoTraspaso> codigosTraspaso = codigoTraspasoService.findByTicketId(ticket.getId());

        for(CodigoTraspaso codigo : codigosTraspaso) {
            codigo.setActivo(false);
            codigoTraspasoService.save(codigo);
        }

        response.put("mensaje", "El ticket ha sido transferido correctamente");

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/codigo/{pCodigo}")
    public ResponseEntity<?> getCodigoParaConfirmacion(@PathVariable String pCodigo) throws Exception {

        Map<String, Object> response = new HashMap<>();

        CodigoTraspaso codigoTraspaso = codigoTraspasoService.findByCodigo(pCodigo);

        if(codigoTraspaso == null){
            response.put("mensaje", "El codigo que intenta confirmar no existe");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if(!codigoTraspaso.isActivo()){
            response.put("mensaje", "Este código de traspaso ya fue utilizado");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Ticket ticket = codigoTraspaso.getTicket();

        response.put("ticket", ticket);
        response.put("localidad", ticket.getLocalidad());
        response.put("evento", ticket.getEvento());
        response.put("codigo", codigoTraspaso);
        response.put("mensaje", "El código es válido para transferir el ticket");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
