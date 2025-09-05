package com.arquitectura.codigo_traspaso.service;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.codigo_traspaso.entity.CodigoTraspaso;
import com.arquitectura.codigo_traspaso.entity.CodigoTraspasoRepository;
import com.arquitectura.mail.service.SendEmailAmazonServiceImpl;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodigoTraspasoServiceImpl extends CommonServiceImpl<CodigoTraspaso, CodigoTraspasoRepository> implements CodigoTraspasoService {

    @Autowired
    private CodigoTraspasoRepository repository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SendEmailAmazonServiceImpl emailService;

    @Override
    public CodigoTraspaso crearCodigoTraspaso(String correo, Ticket ticket, Cliente cliente) throws Exception {

        CodigoTraspaso codigoTraspaso = new CodigoTraspaso(correo, cliente, ticket);

        CodigoTraspaso codigoBd = this.save(codigoTraspaso);

        emailService.mandarCorreoCederTicket(codigoBd);

        return codigoBd;
    }

    @Override
    public CodigoTraspaso findByCodigo(String pIdCodigo) {
        return repository.findByCodigo(pIdCodigo);
    }

    @Override
    public List<CodigoTraspaso> findByTicketId(Long ticketId) {
        return repository.findByTicketId(ticketId);
    }

}
