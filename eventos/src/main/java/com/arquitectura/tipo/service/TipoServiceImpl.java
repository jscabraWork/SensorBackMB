package com.arquitectura.tipo.service;

import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.entity.TipoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Service
public class TipoServiceImpl extends CommonServiceImpl<Tipo, TipoRepository> implements TipoService {
    
    @Value("${tipos-eventos.topic}")
    private String tiposTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    
    @Transactional("transactionManager")
    @Override
    public Tipo save(Tipo pTipo) {

        Tipo tipo = super.save(pTipo);

        TipoEvent tipoEvent = new TipoEvent(
                tipo.getId(),
                tipo.getNombre()
        );
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(tiposTopic, "Tipo-" + tipoEvent.getId(), tipoEvent);
            record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return tipo;
    }

    @Override
    @Transactional("transactionManager")
    public void deleteById(Long pId) {
        Tipo tipo = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún tipo con el id proporcionado"));
        // Si tienes alguna validación específica para Tipo, agrégala aquí

        EntityDeleteEventLong tipoDelete = new EntityDeleteEventLong(tipo.getId());
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(tiposTopic, "Tipo-" + tipo.getId(), tipoDelete);
            record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
            repository.deleteById(tipo.getId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
