package com.arquitectura.ticket.consumer;


import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.events.TicketEvent;
import com.arquitectura.seguro.entity.SeguroRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class TicketEventAdapterImpl extends EventAdapterImpl<Ticket, TicketEvent> implements TicketEventAdapter{

	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TarifaRepository tarifaRepository;

	@Autowired
	private SeguroRepository seguroRepository;

	
	@Override
	public Ticket creacion(Ticket entity, TicketEvent event) {

	    entity.setId(event.getId());
	    entity.setEstado(event.getEstado());
	    entity.setNumero(event.getNumero());
		entity.setTipo(event.getTipo());

	    // Asignar campos adicionales de TicketEvent a Ticket si es necesario
	    if(event.getPalcoId()!=null) {
	    	Ticket padre =ticketRepository.findById(event.getPalcoId()).orElse(null);
	    	entity.setPalco(padre);
	    	if(padre != null) {
	    		entity.setLocalidad(padre.getLocalidad());
	    	}
	    }
	    
	    // Asignar tarifa si no es null
	    if(event.getTarifaId() != null) {
	    	entity.setTarifa(tarifaRepository.findById(event.getTarifaId()).orElse(null));
	    }
	    
	    // Asignar clientes si es necesario
	    if(event.getClienteNumeroDocumento() != null) {
	    	entity.setCliente(clienteRepository.findById(event.getClienteNumeroDocumento()).orElse(null));
	    }
	    
	    // Asignar seguro si no es null
	    if(event.getSeguroId() != null) {
	    	entity.setSeguro(seguroRepository.findById(event.getSeguroId()).orElse(null));
	    }

	    return super.creacion(entity, event);
	}
}
