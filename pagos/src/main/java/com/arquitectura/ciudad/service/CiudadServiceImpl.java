package com.arquitectura.ciudad.service;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.events.CiudadEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.services.CommonServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class CiudadServiceImpl extends CommonServiceImpl<Ciudad, CiudadRepository> implements CiudadService {

    @Value("${ciudades.topic}")
    private String ciudadesTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /*** Crea una ciudad pero verifica primero que no exista una ciudad ya con ese nombre*/
    @Override
    @Transactional("transactionManager")
    public Ciudad crear(Ciudad ciudad) {
        repository.findByNombre(ciudad.getNombre())
                .ifPresent(c -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Ya existe una ciudad con el nombre: " + ciudad.getNombre()
                    );
                });

        return saveKafka(ciudad);
    }

    /*** Modifica una ciudad existente*/
    @Override
    @Transactional("transactionManager")
    public Ciudad actualizar(Long pId, Ciudad ciudad) {
        Optional<Ciudad> cityExists = repository.findById(pId);
        if(cityExists.isEmpty()) {
            throw new EntityNotFoundException("Ciudad no encontrada con ID: " + pId);
        }
        Ciudad actualCity = cityExists.get();
        actualCity.setNombre(ciudad.getNombre());
        return this.saveKafka(actualCity);
    }


    @Transactional("transactionManager")
    @Override
    public Ciudad saveKafka(Ciudad pCiudad) {
        Ciudad ciudad = this.save(pCiudad);
        CiudadEvent ciudadEvent = new CiudadEvent(
                ciudad.getId(),
                ciudad.getNombre()
        );
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ciudadesTopic, "Ciudad-" + ciudadEvent.getId(), ciudadEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
        return ciudad;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Ciudad ciudad = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna ciudad con el id proporcionado"));
        if(ciudad.getVenues() == null || ciudad.getVenues().isEmpty()) {
            EntityDeleteEventLong ciudadDelete = new EntityDeleteEventLong(ciudad.getId());
            try {
                ProducerRecord<String, Object> record = new ProducerRecord<>(ciudadesTopic, "Ciudad-" + ciudad.getId(), ciudadDelete);
                record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();
                repository.deleteById(ciudad.getId());
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No puede eliminar una ciudad que contenga venues");
        }
    }
}

