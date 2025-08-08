package com.arquitectura.orden_promotor.service;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden_promotor.entity.OrdenPromotor;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface OrdenPromotorService extends CommonService<OrdenPromotor> {

    /**
     * Crea una orden de promotor no numerada (localidad general)
     */
    OrdenPromotor crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                       Long pLocalidadId, String pPromotorId) throws Exception;

    /**
     * Crea una orden de promotor numerada (asientos específicos)
     */
    OrdenPromotor crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento,
                                     String pPromotorId) throws Exception;

    /**
     * Crea una orden de promotor para palco individual
     */
    OrdenPromotor crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId,
                                            String pNumeroDocumento, String pPromotorId) throws Exception;

    /**
     * Obtiene todas las órdenes de promotor por número de documento del cliente
     */
    List<OrdenPromotor> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento);

    /**
     * Obtiene todas las órdenes por número de documento del promotor
     */
    List<OrdenPromotor> getAllOrdenesByPromotorNumeroDocumento(String promotorNumeroDocumento);

    /**
     * Guarda una orden de promotor y publica el evento en Kafka
     */
    OrdenPromotor saveKafka(OrdenPromotor pOrden);


    public void publicarVentaPromotor(Orden orden);

}
