package com.arquitectura.codigo_traspaso.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.codigo_traspaso.entity.CodigoTraspaso;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface CodigoTraspasoService extends CommonService<CodigoTraspaso> {

    public CodigoTraspaso crearCodigoTraspaso(String correo, Ticket ticket, Cliente cliente) throws Exception;
    public CodigoTraspaso findByCodigo(String pIdCodigo);

    public List<CodigoTraspaso> findByTicketId(Long ticketId);

}
