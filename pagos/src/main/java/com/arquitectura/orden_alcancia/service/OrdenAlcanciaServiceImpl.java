package com.arquitectura.orden_alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.orden_alcancia.entity.OrdenAlcanciaRepository;
import com.arquitectura.orden_promotor.service.OrdenPromotorService;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.events.OrdenAlcanciaEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.ticket.entity.Ticket;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenAlcanciaServiceImpl extends CommonServiceImpl<OrdenAlcancia, OrdenAlcanciaRepository> implements OrdenAlcanciaService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AlcanciaService alcanciaService;

    @Autowired
    private OrdenPromotorService ordenPromotorService;

    @Value("${ordenes.alcancias.topic}")
    private String ordenesAlcanciasTopic;

    /**
     * Confirma la creación de una orden alcancía y realiza las operaciones necesarias
     * @param orden La orden a confirmar
     * @return La orden alcancía confirmada
     */
    @Override
    @Transactional("transactionManager")
    public void confirmarCreacion(Orden orden, Double pAporte) throws Exception {

        // Validar el estado de los tickets de la orden
        //Solo agregar a la alcancía los tickets que no estén en estado 1 (vendido) o 2 (reservado)
        List<Ticket> tickets = orden.getTickets().stream()
                .filter(t -> t.getEstado() != 1 && t.getEstado() != 2).toList();

        //Abrir nueva alcancía con los tickets filtrados
        //el método crear maneja el aporte inicial de la alcancía, reserva los tickets, la persiste y publica los eventos en Kafka
        Alcancia alcancia = alcanciaService.crear(orden.getCliente(), tickets, orden.calcularValorOrden(), orden.getTarifa());

        //Aportar a la alcancía el aporte inicial
        alcanciaService.aportar(alcancia, pAporte);

        // Crear OrdenAlcancia a partir de la orden y la alcancía
        OrdenAlcancia ordenAlcancia = new OrdenAlcancia(orden.getId(), alcancia);

        //Confirmar la orden alcancía
        ordenAlcancia.confirmar();

        //Guardar la orden
        OrdenAlcancia ordenBD = saveKafka(ordenAlcancia);

        //Publicar venta de promotor si corresponde
        ordenPromotorService.publicarVentaPromotor(ordenBD);

    }

    @Override
    @Transactional("transactionManager")
    public void confirmarAporte(Orden orden, Double pAporte) throws Exception {

        OrdenAlcancia ordenAlcancia = repository.findById(orden.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden alcancía con el id proporcionado"));

        Alcancia alcancia = ordenAlcancia.getAlcancia();
        alcanciaService.aportar(alcancia, pAporte);
        ordenAlcancia.confirmar();
        saveKafka(ordenAlcancia);
    }


    //----------------Métodos para Kafka-------------------

    /**
     * Guarda una orden alcancía y publica el evento en Kafka
     * @param pOrdenAlcancia La orden alcancía a guardar
     * @return La orden alcancía guardada
     */
    @Transactional("transactionManager")
    @Override
    public OrdenAlcancia saveKafka(OrdenAlcancia pOrdenAlcancia) {
        OrdenAlcancia ordenAlcancia = this.save(pOrdenAlcancia);

        OrdenAlcanciaEvent ordenAlcanciaEvent = new OrdenAlcanciaEvent(
                ordenAlcancia.getId(),
                ordenAlcancia.getEstado(),
                ordenAlcancia.getTipo(),
                ordenAlcancia.getEvento() != null ? ordenAlcancia.getEvento().getId() : null,
                ordenAlcancia.getValorOrden(),
                ordenAlcancia.getValorSeguro(),
                ordenAlcancia.getTickets() != null ? ordenAlcancia.getTickets().stream().map(t -> t.getId()).toList() : List.of(),
                ordenAlcancia.getCliente() != null ? ordenAlcancia.getCliente().getNumeroDocumento() : null,
                ordenAlcancia.getTarifa() != null ? ordenAlcancia.getTarifa().getId() : null,
                ordenAlcancia.getAlcancia() != null ? ordenAlcancia.getAlcancia().getId() : null
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesAlcanciasTopic, "OrdenAlcancia-" + ordenAlcanciaEvent.getId(), ordenAlcanciaEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return ordenAlcancia;
    }

    /**
     * Elimina una orden alcancía por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la orden alcancía a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        OrdenAlcancia ordenAlcancia = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden alcancía con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesAlcanciasTopic, "OrdenAlcancia-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

}
