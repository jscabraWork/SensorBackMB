package com.arquitectura.transaccion.service;


import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class TransaccionServiceImpl extends CommonServiceImpl<Transaccion, TransaccionRepository> implements TransaccionService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${transacciones.topic}")
    private String transaccionesTopic;

    @Override
    public Transaccion getTransaccionRepetida(int status, Long orden) {
        return repository.findByStatusAndOrdenId(status, orden);
    }



    //----------------Métodos para Kafka-------------------
    /**
     * Guarda una transacción y publica el evento en Kafka
     * @param pTransaccion La transacción a guardar
     * @return La transacción guardada
     */
    @Transactional("transactionManager")
    @Override
    public Transaccion saveKafka(Transaccion pTransaccion) {
        Transaccion transaccion = this.save(pTransaccion);

        TransaccionEvent transaccionEvent = new TransaccionEvent(
                transaccion.getId(),
                transaccion.getAmount(),
                transaccion.getEmail(),
                transaccion.getFullName(),
                transaccion.getIdPasarela(),
                transaccion.getIdPersona(),
                transaccion.getIp(),
                transaccion.getMetodo(),
                transaccion.getMetodoNombre(),
                transaccion.getPhone(),
                transaccion.getStatus(),
                transaccion.getIdBasePasarela(),
                transaccion.getOrden() != null ? transaccion.getOrden().getId() : null
        );
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(transaccionesTopic, "Transaccion-" + transaccionEvent.getId(), transaccionEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
        return transaccion;
    }

    /**
     * Elimina una transacción por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la transacción a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Transaccion transaccion = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna transacción con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(transaccionesTopic, "Transaccion-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

    @Transactional("transactionManager")
    @Override
    public Transaccion crearTransaccionPuntoFisico(OrdenPuntoFisico orden, Integer metodo, Integer status){

        Cliente cliente = orden.getCliente();

        Transaccion transaccion = new Transaccion(
                orden.getValorOrden(),
                cliente.getCorreo(),
                cliente.getNombre(),
                cliente.getNumeroDocumento(),
                metodo,
                (cliente.getCelular() != null && !cliente.getCelular().isEmpty()) ? cliente.getCelular() : "No aplica",
                status,
                orden
        );

        return saveKafka(transaccion);
    }

    @Override
    public void saveAllKafka(List<Transaccion> pTransaccion) {
        pTransaccion.forEach(this::saveKafka);
    }

    @Override
    public Page<Transaccion> findByFiltro(String numeroDocumento, String correo, Date fechaInicio, 
                                         Date fechaFin, Integer estado, Integer metodo, String metodoNombre, Pageable pageable) {
        return repository.findByFiltro(numeroDocumento, correo, fechaInicio, fechaFin, estado, metodo, metodoNombre, pageable);
    }

}
