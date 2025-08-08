package com.arquitectura.seguro.service;

import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.seguro.entity.SeguroRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.events.SeguroEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeguroServiceImpl extends CommonServiceImpl<Seguro, SeguroRepository> implements SeguroService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${seguros.topic}")
    private String segurosTopic;

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda un seguro y publica el evento en Kafka
     * @param pSeguro El seguro a guardar
     * @return El seguro guardado
     */
    @Transactional("transactionManager")
    @Override
    public Seguro saveKafka(Seguro pSeguro) {
        Seguro seguro = this.save(pSeguro);

        SeguroEvent seguroEvent = new SeguroEvent(
                seguro.getId(),
                seguro.getValor(),
                seguro.isReclamado()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(segurosTopic, "Seguro-" + seguroEvent.getId(), seguroEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return seguro;
    }

    /**
     * Elimina un seguro por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del seguro a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Seguro seguro = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún seguro con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(segurosTopic, "Seguro-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

}
