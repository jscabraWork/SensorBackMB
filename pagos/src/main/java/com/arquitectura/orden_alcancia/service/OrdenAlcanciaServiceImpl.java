package com.arquitectura.orden_alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.localidad.entity.Localidad;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Service
public class OrdenAlcanciaServiceImpl extends CommonServiceImpl<OrdenAlcancia, OrdenAlcanciaRepository> implements OrdenAlcanciaService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AlcanciaService alcanciaService;

    @Autowired
    private OrdenPromotorService ordenPromotorService;

    @PersistenceContext
    private EntityManager entityManager;

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
                .filter(t -> t.getEstado() != 1 && t.getEstado() != 2)
                .collect(java.util.stream.Collectors.toList());

        //Abrir nueva alcancía con los tickets filtrados
        //el método crear maneja el aporte inicial de la alcancía, reserva los tickets, la persiste y publica los eventos en Kafka
        Alcancia alcancia = alcanciaService.crear(orden.getCliente(), tickets, orden.calcularValorOrden(), orden.getTarifa());

        //Aportar a la alcancía el aporte inicial
        alcanciaService.aportar(alcancia, pAporte);

        // Insertar registro en ordenes_alcancia usando el ID de la orden existente
        repository.insertOrdenAlcancia(orden.getId(), alcancia.getId());
        
        // Ahora recuperar la OrdenAlcancia creada y publicar evento
        OrdenAlcancia ordenAlcancia = repository.findById(orden.getId())
                .orElseThrow(() -> new RuntimeException("Error al crear la orden alcancía con ID: " + orden.getId()));
        
        //Confirmar la orden alcancía (actualizar estado en tabla ordenes)
        ordenAlcancia.confirmar();
        
        //Guardar cambios y publicar evento
        OrdenAlcancia ordenBD = saveKafka(ordenAlcancia);

        // Flush para asegurar visibilidad antes de buscar OrdenPromotor
        //NO BORRAR
        //Atte: Isaac
        entityManager.flush();
        entityManager.clear();
        
        //Publicar venta de promotor si corresponde
        ordenPromotorService.publicarVentaPromotor(ordenBD);

    }

    @Override
    @Transactional("transactionManager")
    public void confirmarAporte(Orden orden, Double pAporte) throws Exception {

        OrdenAlcancia ordenAlcancia = repository.findById(orden.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden alcancía con el id proporcionado"));

        Alcancia alcancia = ordenAlcancia.getAlcancia();
        Alcancia alcanciaAporte = alcanciaService.aportar(alcancia, pAporte);
        ordenAlcancia.confirmar();
        alcanciaService.saveKafka(alcanciaAporte);
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

        //Si y solo si la orden es creacion de alcancaia, se debe agregar el evento de alcancía
        //para evitar problemas de asincronia al consumirla, ya que la alcancia y la orden se crean en el mismo momento
        if(ordenAlcancia.getTipo()==3){
            //Obtener la alcancía asociada a la orden alcancía
            Alcancia alcancia = ordenAlcancia.getAlcancia();

            AlcanciaEvent alcanciaEvent = new AlcanciaEvent(
                    alcancia.getCreationDate(),
                    alcancia.getLastModifiedDate(),
                    alcancia.getId(),
                    alcancia.getPrecioParcialPagado(),
                    alcancia.getPrecioTotal(),
                    alcancia.isActiva(),
                    alcancia.getCliente() != null ? alcancia.getCliente().getNumeroDocumento() : null,
                    alcancia.getTickets() != null ? alcancia.getTickets().stream().map(t -> t.getId()).toList() : List.of()
            );
            ordenAlcanciaEvent.setAlcanciaEvent(alcanciaEvent);
        }

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

    @Override
    public OrdenAlcancia crearOrdenAporte(Long pAlcanciaId, Double pAporte) throws Exception {

        Alcancia alcancia = alcanciaService.findById(pAlcanciaId);

        if(alcancia ==null || !alcancia.isActiva()){
            throw new IllegalArgumentException("La alcancía no existe o no está activa");
        }

        Localidad localidad = alcancia.getTickets().get(0).getLocalidad();

        //Validar aporte minimo
        if(pAporte< localidad.getAporteMinimo()){
            throw new IllegalArgumentException("El aporte debe ser mayor o igual al aporte mínimo de la localidad: " + localidad.getAporteMinimo());
        }

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia(alcancia,pAporte,localidad);

        return saveKafka(ordenAlcancia);

    }

}
