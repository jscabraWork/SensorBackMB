package com.arquitectura.orden_puntofisico.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_puntofisico.service.OrdenPuntoFisicoService;
import com.arquitectura.ticket.entity.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ordenes-puntosfisicos")
public class OrdenPuntoFisicoController extends CommonController<OrdenPuntoFisico, OrdenPuntoFisicoService> {

    @PreAuthorize("hasRole('PUNTO')")
    @PostMapping("/crear-no-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNoNumerados(@RequestParam Long pLocalidadId,
                                                   @RequestParam Long pEventoId,
                                                   @RequestParam String pClienteNumeroDocumento,
                                                   @RequestParam Integer pCantidad,
                                                   @RequestParam String pPuntoFisicoId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPuntoFisico orden = service.crearOrdenNoNumerada(pCantidad, pEventoId, pClienteNumeroDocumento, pLocalidadId, pPuntoFisicoId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PUNTO')")
    @PostMapping("/crear-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNumerados(@RequestBody List<Ticket> pTickets,
                                                 @RequestParam Long pEventoId,
                                                 @RequestParam String pClienteNumeroDocumento,
                                                 @RequestParam String pPuntoFisicoId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPuntoFisico orden = service.crearOrdenNumerada(pTickets, pEventoId, pClienteNumeroDocumento, pPuntoFisicoId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PUNTO')")
    @PostMapping("/crear-individual")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenIndividuales(@RequestParam Long pTicketPadreId,
                                                    @RequestParam Integer pCantidad,
                                                    @RequestParam Long pEventoId,
                                                    @RequestParam String pClienteNumeroDocumento,
                                                    @RequestParam String pPuntoFisicoId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPuntoFisico orden = service.crearOrdenPalcoIndividual(pTicketPadreId, pCantidad, pEventoId, pClienteNumeroDocumento, pPuntoFisicoId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Obtiene todas las órdenes de punto físico por número de documento del cliente
     */
    @PreAuthorize("hasRole('PUNTO')")
    @GetMapping("/ordenes/cliente/{numeroDocumento}")
    public ResponseEntity<?> getOrdenesByClienteId(@PathVariable String numeroDocumento) {
        Map<String, Object> response = new HashMap<>();
        response.put("ordenes", service.getAllOrdenesByClienteNumeroDocumento(numeroDocumento));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Obtiene todas las órdenes por número de documento del punto físico
     */
    @PreAuthorize("hasRole('PUNTO')")
    @GetMapping("/ordenes/punto-fisico/{puntoFisicoNumeroDocumento}")
    public ResponseEntity<?> getOrdenesByPuntoFisicoId(@PathVariable String puntoFisicoNumeroDocumento) {
        Map<String, Object> response = new HashMap<>();
        response.put("ordenes", service.getAllOrdenesByPuntoFisicoNumeroDocumento(puntoFisicoNumeroDocumento));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('PUNTO')")
    @PostMapping("/confirmar")
    @Transactional("transactionManager")
    public ResponseEntity<?> confirmarOrdenPuntoFisico(@RequestParam Long pOrdenId, @RequestParam Integer pMetodo) throws Exception {

        Map<String, Object> response = new HashMap<>();

        OrdenPuntoFisico ordenPuntoFisico = service.confirmar(pOrdenId, pMetodo);

        return new ResponseEntity<>(ordenPuntoFisico, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('PUNTO')")
    @PostMapping("/cancelar")
    @Transactional("transactionManager")
    public ResponseEntity<?> cancelarOrdenPuntoFisico(@RequestParam Long pOrdenId) throws Exception {
        Map<String, Object> response = new HashMap<>();
        service.cancelar(pOrdenId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
