package com.arquitectura.orden.service;

import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.cupon.entity.Cupon;
import com.arquitectura.cupon.service.CuponService;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.orden.helper.OrdenCreationHelper;
import com.arquitectura.orden_promotor.service.OrdenPromotorService;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.transaccion.service.TransaccionService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdenServiceImpl extends CommonServiceImpl<Orden, OrdenRepository> implements OrdenService {

    @Autowired
    private OrdenCreationHelper creationHelper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private TransaccionRepository trxRepository;

    @Autowired
    private ConfigSeguroService configSeguroService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ordenes.topic}")
    private String ordenesTopic;

    @Autowired
    private OrdenPromotorService ordenPromotorService;

    @Autowired
    private CuponService cuponService;

    @Autowired
    private TransaccionService transaccionService;

    //Logger para registro de errores en consola
    private static final Logger logger = LoggerFactory.getLogger(OrdenServiceImpl.class);

    @Override
    public Orden actualizarEstado(Long pId, int estado){

        Orden orden = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontro ninguna orden con el id proporcionado"));
        orden.setEstado(estado);

        orden.getTransacciones().forEach(tr->{tr.setStatus(estado);});

        ticketRepository.saveAll(orden.getTickets());

        trxRepository.saveAll(orden.getTransacciones());

        repository.save(orden);

        return orden;
    }

    @Override
    public Orden agregarTicketAOrden(Long ordenId, Long idTicket){

        Orden orden = repository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("No se encontro ninguna orden con el id proporcionado"));

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("No se encontro ningun ticket con el id proporcionado"));

        if(orden.getTickets().contains(ticket)) {
            throw new RuntimeException("El ticket ya está asociado a esta orden");
        }

        orden.getTickets().add(ticket);

        return repository.save(orden);
    }


    /*** Elimina un ticket existente de una orden*/
    @Override
    public void deleteTicketFromOrden(Long pIdOrden, Long pIdTicket) {

        Orden orden = repository.findById(pIdOrden)
                .orElseThrow(() -> new RuntimeException("No se encontro ninguna orden con el id proporcionado"));

        Ticket ticket = ticketRepository.findById(pIdTicket)
                .orElseThrow(() -> new RuntimeException("No se encontro ningun ticket con el id proporcionado"));

        orden.getTickets().remove(ticket);

        ticket.getOrdenes().remove(orden);

        ticketRepository.save(ticket);

        repository.save(orden);
    }


    /**
     * Trae todos las ordenes por el cliente id
     * @param numeroDocumento El ID del cliente
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    @Override
    public List<Orden> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento){
        return repository.findByClienteNumeroDocumento(numeroDocumento);
    }

    @Transactional("transactionManager")
    @Override
    public Orden crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento, Long pLocalidadId) throws Exception {

        // Factory para crear Orden regular
        OrdenCreationHelper.OrdenFactory<Orden> factory =(evento, cliente, tickets) ->
                new Orden(evento, cliente, tickets, null);

        Orden orden = creationHelper.crearOrdenNoNumerada(
                pCantidad,
                pEventoId,
                pNumeroDocumento,
                pLocalidadId,
                factory);

        //publicar estado en proceso de los tickets
        ticketService.saveAllKafka(orden.getTickets());

        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public Orden crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento) throws Exception {

        OrdenCreationHelper.OrdenFactory<Orden> factory = (evento, cliente, ticketsList) ->
            new Orden(evento, cliente, ticketsList, null);

        Orden orden = creationHelper.crearOrdenNumerada(
                tickets,
                pEventoId,
                pNumeroDocumento,
                factory);

        ticketService.saveAllKafka(orden.getTickets());
        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public Orden crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId, String pNumeroDocumento) throws Exception {

        OrdenCreationHelper.OrdenFactory<Orden> factory =(evento, cliente, ticketsList) ->
                new Orden(evento, cliente, ticketsList, null);

        Orden orden = creationHelper.crearOrdenPalcoIndividual(
                pTicketPadreId,
                pCantidad,
                pEventoId,
                pNumeroDocumento,
                factory);

        ticketService.saveAllKafka(orden.getTickets());
        return this.saveKafka(orden);
    }


    @Transactional("transactionManager")
    @Override
    public Orden confirmar(Orden orden) throws Exception {

        orden.confirmar();

        //Enviar publica los tickets en kafka
        ticketService.enviar(orden.getTickets());

        Orden ordenBD = saveKafka(orden);

        ordenPromotorService.publicarVentaPromotor(ordenBD);

        return ordenBD;
    }

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda una orden y publica el evento en Kafka
     * @param pOrden La orden a guardar
     * @return La orden guardada
     */
   @Transactional("transactionManager")
   @Override
   public Orden saveKafka(Orden pOrden) {

       Orden orden = this.save(pOrden);

       OrdenEvent ordenEvent = new OrdenEvent(
               orden.getId(),
               orden.getEstado(),
               orden.getTipo(),
               orden.getEvento() != null ? orden.getEvento().getId() : null,
               orden.getValorOrden(),
               orden.getValorSeguro(),
               orden.getTickets() != null ? orden.getTickets().stream().map(Ticket::getId).collect(Collectors.toList()) : List.of(),
               orden.getCliente() != null ? orden.getCliente().getNumeroDocumento() : null,
               orden.getTarifa() != null ? orden.getTarifa().getId() : null
       );

       try {
           ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "Orden-" + ordenEvent.getId(), ordenEvent);
           record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
           kafkaTemplate.send(record).get();
       } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
           e.printStackTrace();
       }
       return orden;
   }

   /**
    * Elimina una orden por su ID y publica el evento de eliminación en Kafka
    * @param pId El ID de la orden a eliminar
    */
   @Transactional("transactionManager")
   @Override
   public void deleteById(Long pId) {

       Orden orden = repository.findById(pId)
               .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden con el id proporcionado"));

       EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
       deleteEvent.setId(pId);

       try {
           ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "Orden-Delete-" + pId, deleteEvent);
           record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
           kafkaTemplate.send(record).get();
       } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
           e.printStackTrace();
       }

       repository.deleteById(pId);
   }

    @Override
    public String aplicarCupon(String pCuponId, Long pOrdenId) {
        String response = "";

        Orden orden = findById(pOrdenId);


        if (orden == null) {
            response = "No se encontró ninguna orden para aplicar el cupón";
            return response;
        }

        Tarifa tarifa = orden.getTarifa();

        //Encontrar el cupón por su ID y validar que sea válido
        //El service validar cupon valida la fecha de vigencia y si esta activo solamente
        Cupon cupon = cuponService.validarCupon(pCuponId, tarifa, orden.getTickets().size() );

        if (cupon == null) {
            response = "El cupón no es válido";
            return response;
        }

        orden.setTarifa(cupon.getTarifa());
        orden.setValorOrden(orden.calcularValorOrden());

        saveKafka(orden);

        return "Cupón aplicado correctamte";
    }


    @Transactional("transactionManager")
    @Override
    public void rechazar(Orden orden) {
        orden.rechazar();
        // Guardar la orden actualizada
        saveKafka(orden);
        transaccionService.saveAllKafka(orden.getTransacciones());
        ticketService.saveAllKafka(orden.getTickets());
    }

    @Override
    public List<Orden> findByEstado(Integer estado) {
        return repository.findByEstado(estado);
    }

    @Override
    public List<Orden> findAllOrdenesSinConfirmacion() {
        return repository.findAllOrdenesSinConfirmacion();
    }


}
