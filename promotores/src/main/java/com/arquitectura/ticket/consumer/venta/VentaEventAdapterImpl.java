package com.arquitectura.ticket.consumer.venta;


import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TicketEvent;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VentaEventAdapterImpl extends EventAdapterImpl<Ticket, TicketEvent> implements VentaEventAdapter {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TarifaRepository tarifaRepository;

	@Autowired
	private LocalidadRepository localidadRepository;

	
	@Override
	public Ticket creacion(Ticket entity, TicketEvent event) {

	    entity.setId(event.getId());
	    entity.setEstado(event.getEstado());
	    entity.setNumero(event.getNumero());
		entity.setTipo(event.getTipo());

	    // Asignar campos adicionales de TicketEvent a Ticket si es necesario
		if(event.getPalcoId()!=null) {
			Ticket padre =ticketRepository.findById(event.getPalcoId()).orElse(null);
			if(padre != null) {
				entity.setPalco(padre);
			}
		}

		if (event.getLocalidadId() != null) {
			entity.setLocalidad(localidadRepository.findById(event.getLocalidadId()).orElse(null));
		}
		if (event.getTarifaId() != null) {
			entity.setTarifa(tarifaRepository.findById(event.getTarifaId()).orElse(null));
		}

	    return super.creacion(entity, event);
	}
}
