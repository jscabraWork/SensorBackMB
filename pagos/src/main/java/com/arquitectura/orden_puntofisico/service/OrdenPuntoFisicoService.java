package com.arquitectura.orden_puntofisico.service;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface OrdenPuntoFisicoService extends CommonService<OrdenPuntoFisico> {

    /**
     * Crea una orden de punto físico no numerada (localidad general)
     */
    OrdenPuntoFisico crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                          Long pLocalidadId, String pPuntoFisicoId) throws Exception;

    /**
     * Crea una orden de punto físico numerada (asientos específicos)
     */
    OrdenPuntoFisico crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento,
                                        String pPuntoFisicoId) throws Exception;

    /**
     * Crea una orden de punto físico para palco individual
     */
    OrdenPuntoFisico crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId,
                                               String pNumeroDocumento, String pPuntoFisicoId) throws Exception;

    /**
     * Crea una orden de punto físico no numerada con una tarifa específica
     */
    OrdenPuntoFisico crearOrdenNoNumeradaConTarifa(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                                   Long pLocalidadId, Long pTarifaId, String pPuntoFisicoId) throws Exception;

    /**
     * Obtiene todas las órdenes de punto físico por número de documento del cliente
     */
    List<OrdenPuntoFisico> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento);

    /**
     * Obtiene todas las órdenes por número de documento del punto físico
     */
    List<OrdenPuntoFisico> getAllOrdenesByPuntoFisicoNumeroDocumento(String puntoFisicoNumeroDocumento);

    /**
     * Guarda una orden de punto físico y publica el evento en Kafka
     */
    OrdenPuntoFisico saveKafka(OrdenPuntoFisico pOrden);

    OrdenPuntoFisico confirmar(Long pOrdenId, Integer pMetodo) throws Exception;

    public void cancelar(Long pOrdenId) throws Exception;

}
