package com.arquitectura.alcancia.consumer;
import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class AlcanciaEventAdapterImpl extends EventAdapterImpl<Alcancia, AlcanciaEvent> implements AlcanciaEventAdapter{

	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private TicketRepository ticketRepository;
	@Override
	public Alcancia creacion(Alcancia entity, AlcanciaEvent event) {

		entity.setId(event.getId());
		entity.setPrecioParcialPagado(event.getPrecioParcialPagado());
		entity.setPrecioTotal(event.getPrecioTotal());
		entity.setEstado(event.getEstado());
		entity.setCliente(clienteRepository.findById(event.getClienteNumeroDocumento()).orElse(null));
		if(event.getTicketsIds()!= null && !event.getTicketsIds().isEmpty()) {
			entity.setTickets(ticketRepository.findAllById(event.getTicketsIds()));
		}
		return super.creacion(entity, event);
	}
}
