package com.arquitectura.orden_promotor.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_promotor.entity.OrdenPromotor;
import com.arquitectura.orden_promotor.service.OrdenPromotorService;
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
@RequestMapping("/ordenes-promotores")
public class OrdenPromotorController extends CommonController<OrdenPromotor, OrdenPromotorService> {

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/crear-no-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNoNumerados(@RequestParam Long pLocalidadId,
                                                   @RequestParam Long pEventoId,
                                                   @RequestParam String pClienteNumeroDocumento,
                                                   @RequestParam Integer pCantidad,
                                                   @RequestParam String pPromotorId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPromotor orden = service.crearOrdenNoNumerada(pCantidad, pEventoId, pClienteNumeroDocumento, pLocalidadId, pPromotorId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/crear-numerada")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenNumerados(@RequestBody List<Ticket> pTickets,
                                                 @RequestParam Long pEventoId,
                                                 @RequestParam String pClienteNumeroDocumento,
                                                 @RequestParam String pPromotorId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPromotor orden = service.crearOrdenNumerada(pTickets, pEventoId, pClienteNumeroDocumento, pPromotorId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/crear-individual")
    @Transactional("transactionManager")
    public ResponseEntity<?> crearOrdenIndividuales(@RequestParam Long pTicketPadreId,
                                                    @RequestParam Integer pCantidad,
                                                    @RequestParam Long pEventoId,
                                                    @RequestParam String pClienteNumeroDocumento,
                                                    @RequestParam String pPromotorId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        OrdenPromotor orden = service.crearOrdenPalcoIndividual(pTicketPadreId, pCantidad, pEventoId, pClienteNumeroDocumento, pPromotorId);
        response.put("ordenId", orden.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
