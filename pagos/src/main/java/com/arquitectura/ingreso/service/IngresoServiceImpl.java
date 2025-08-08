package com.arquitectura.ingreso.service;

import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngresoServiceImpl extends CommonServiceImpl<Ingreso, IngresoRepository> implements IngresoService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ingresos.topic}")
    private String ingresosTopic;

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda un ingreso y publica el evento en Kafka
     * @param pIngreso El ingreso a guardar
     * @return El ingreso guardado
     */
    @Transactional("transactionManager")
    @Override
    public Ingreso saveKafka(Ingreso pIngreso) {
        Ingreso ingreso = this.save(pIngreso);

        IngresoEvent ingresoEvent = new IngresoEvent(
                ingreso.getId(),
                ingreso.isUtilizado(),
                ingreso.getTicket() != null ? ingreso.getTicket().getId() : null,
                ingreso.getDia() != null ? ingreso.getDia().getId() : null,
                ingreso.getFechaIngreso()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ingresosTopic, "Ingreso-" + ingresoEvent.getId(), ingresoEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return ingreso;
    }

    /**
     * Elimina un ingreso por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del ingreso a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Ingreso ingreso = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún ingreso con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ingresosTopic, "Ingreso-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

}
