package com.arquitectura.alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.dto.MisAlcanciasDto;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.qr.service.QRService;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlcanciaServiceImpl extends CommonServiceImpl<Alcancia, AlcanciaRepository> implements AlcanciaService {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private QRService qrService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${alcancias.topic}")
    private String alcanciasTopic;

    @Override
    @Transactional("transactionManager")
    public Alcancia crear(Cliente cliente, List<Ticket> tickets, Double precioTotal, Tarifa tarifa) throws Exception {

        //Crear nueva alcancia con los datos de la orden y el dinero pagado
        Alcancia alcancia = new Alcancia(
                cliente,
                tickets,
                precioTotal, // Valor total de la orden
                0.0, // Valor parcial pagado (Crear con valor 0.0)+
                tarifa
        );
        ticketService.saveAllKafka(tickets);
        return save(alcancia);
    }


    @Override
    @Transactional("transactionManager")
    public Alcancia aportar(Alcancia alcancia, Double aporte) throws Exception {

        // Localidad de la alcancia para validar aporte minimo
        Localidad localidad = alcancia.getTickets().get(0).getLocalidad();

        // Validar que el aporte sea válido y la alcancía esté activa
        if (aporte <= 0) {
            throw new IllegalArgumentException("El aporte debe ser mayor a 0");
        }

        if (!alcancia.isActiva()) {
            throw new IllegalStateException("No se puede aportar a una alcancía inactiva");
        }

        //Validar aporte minimo
        if(aporte< localidad.getAporteMinimo()){
            throw new IllegalArgumentException("El aporte debe ser mayor o igual al aporte mínimo de la localidad: " + localidad.getAporteMinimo());
        }

        double dineroActual = alcancia.getPrecioParcialPagado() + aporte;
        
        Cliente cliente = alcancia.getCliente();
        int contador = 0;
        List<Ticket> tickets = alcancia.getTickets();
        
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            boolean venderBoleta = false;

            //VALIDACION ADICIONAL
            //Si el ticket no tiene tarifa, no se puede vender, rompemos el ciclo
            if(ticket.getTarifa()==null){
                contador++;
                continue;
            }

            double precioTotalBoleta = ticket.getPrecio();
            venderBoleta = (contador * precioTotalBoleta + precioTotalBoleta) <= dineroActual;

            if (ticket.getEstado() != 1 && venderBoleta) {
                ticket.vender(cliente, ticket.getTarifa());
                ticketService.saveKafka(ticket);
                qrService.mandarQR(ticket);
                contador++;
            } else {
                contador++;
            }
        }
        alcancia.aportar(aporte);
        
        return save(alcancia);
    }

    @Override
    public List<Alcancia> findActivasByCliente(String pClienteId) {
        return repository.findByClienteNumeroDocumentoAndEstado(pClienteId, 1);
    }

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda una alcancía y publica el evento en Kafka
     * @param pAlcancia La alcancía a guardar
     * @return La alcancía guardada
     */
    @Transactional("transactionManager")
    @Override
    public Alcancia saveKafka(Alcancia pAlcancia) {
        Alcancia alcancia = this.save(pAlcancia);

        AlcanciaEvent alcanciaEvent = new AlcanciaEvent(
                alcancia.getCreationDate(),
                alcancia.getLastModifiedDate(),
                alcancia.getId(),
                alcancia.getPrecioParcialPagado(),
                alcancia.getPrecioTotal(),
                alcancia.getEstado(),
                alcancia.getCliente() != null ? alcancia.getCliente().getNumeroDocumento() : null,
                alcancia.getTickets() != null ? alcancia.getTickets().stream().map(t -> t.getId()).toList() : List.of()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(alcanciasTopic, "Alcancia-" + alcanciaEvent.getId(), alcanciaEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return alcancia;
    }

    /**
     * Elimina una alcancía por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la alcancía a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {

        Alcancia alcancia = repository.findById(pId).orElseThrow(() -> new RuntimeException("No se encontró ninguna alcancía con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(alcanciasTopic, "Alcancia-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

    @Override
    public List<MisAlcanciasDto> getMisAlcanciasByCliente(String numeroDocumento) {
        List<Alcancia> alcancias = repository.findByClienteNumeroDocumentoAndEstado(numeroDocumento, 1);
        return MisAlcanciasDto.AlcanciastoDto(alcancias);
    }

}
