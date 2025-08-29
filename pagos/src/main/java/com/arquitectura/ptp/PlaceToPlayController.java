package com.arquitectura.ptp;

import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PlaceToPlayController {

    private static final Logger logger = LoggerFactory.getLogger(PlaceToPlayController.class);

    @Autowired
    private PlaceToPlayService service;

    @PreAuthorize("hasRole('CLIENTE')")
    @Transactional("transactionManager")
    @PostMapping("/ptp/crear-link")
    public ResponseEntity<?> crearLinkPago(@RequestParam("idOrden") Long pIdOrden,
                                     @RequestParam("url") String returnUrl,
                                     @RequestParam("seguro") boolean seguro,
                                     @RequestParam(required = false, name = "aporteAlcancia") Double aporteAlcancia) {

        logger.info("Iniciando creación de link para orden ID: {}, seguro: {}", pIdOrden, seguro);

        try {
            ProcessUrlResponse response = service.crearEnlacePago(pIdOrden, returnUrl, seguro, aporteAlcancia);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            logger.error("Error al crear enlace de pago para orden {}: {}", pIdOrden, e.getMessage(), e);
            return new ResponseEntity<>("Error al procesar la solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/ptp/recepcion-link")
    @Transactional("transactionManager")
    public ResponseEntity<?> recibirTransaccionPTP(@RequestBody SessionNotification data) {
        
        logger.info("Recibiendo notificación PTP para requestId: {}", data.getRequestId());
        try {
            return service.procesarNotificacionPTP(data);
        } catch (Exception e) {
            logger.error("Error al procesar notificación PTP: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}