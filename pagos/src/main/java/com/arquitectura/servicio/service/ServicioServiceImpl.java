package com.arquitectura.servicio.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.servicio.entity.ServicioRepository;
import com.arquitectura.events.ServicioEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioServiceImpl extends CommonServiceImpl<Servicio, ServicioRepository> implements ServicioService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${servicios.topic}")
    private String serviciosTopic;

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda un servicio y publica el evento en Kafka
     *
     * @param pServicio El servicio a guardar
     * @return El servicio guardado
     */
    @Transactional("transactionManager")
    @Override
    public Servicio saveKafka(Servicio pServicio) {
        Servicio servicio = this.save(pServicio);

        ServicioEvent servicioEvent = new ServicioEvent(
                servicio.getId(),
                servicio.getNombre(),
                servicio.isUtilizado(),
                servicio.getTicket() != null ? servicio.getTicket().getId() : null
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(serviciosTopic, "Servicio-" + servicioEvent.getId(), servicioEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return servicio;
    }

    /**
     * Elimina un servicio por su ID y publica el evento de eliminación en Kafka
     *
     * @param pId El ID del servicio a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Servicio servicio = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún servicio con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(serviciosTopic, "Servicio-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
    }

}
