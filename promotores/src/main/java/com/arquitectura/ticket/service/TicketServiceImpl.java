package com.arquitectura.ticket.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl extends CommonServiceImpl<Ticket, TicketRepository> implements TicketService {
    
    @Override
    public List<Ticket> findByEventoIdAndPromotorNumeroDocumentoAndEstado(
            Long eventoId, 
            String promotorNumeroDocumento, 
            Integer estado) {
        return repository.findByLocalidad_Dias_Evento_IdAndPromotor_NumeroDocumentoAndEstado(
                eventoId, 
                promotorNumeroDocumento, 
                estado
        );
    }

    @Override
    public List<Ticket> findVentasByPromotorAndEvento(Long eventoId, String promotorNumeroDocumento) {
        return repository.findByLocalidad_Dias_Evento_IdAndPromotor_NumeroDocumentoAndEstadoIn(eventoId,
                promotorNumeroDocumento,
                List.of(1, 2)
        );
    }
}
