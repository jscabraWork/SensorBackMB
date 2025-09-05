package com.arquitectura.orden_traspaso.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;

public interface OrdenTraspasoService extends CommonService<OrdenTraspaso> {

    public OrdenTraspaso transferirTicket(Ticket ticket, Cliente cliente, Cliente receptor) throws Exception;

}
