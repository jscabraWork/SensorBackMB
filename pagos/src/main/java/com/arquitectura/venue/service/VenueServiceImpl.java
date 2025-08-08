package com.arquitectura.venue.service;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.VenueEvent;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class VenueServiceImpl extends CommonServiceImpl<Venue, VenueRepository> implements VenueService {

    @Value("${venues.topic}")
    private String venuesTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private CiudadRepository ciudadRepository;

    /*** Crea un venue pero verifica primero que no exista un venue ya con ese nombre en la misma ciudad*/
    @Transactional("transactionManager")
    public Venue createVenue(Long ciudadId, Venue venue) {

        repository.findByNombreAndCiudadId(venue.getNombre(), ciudadId)
                .ifPresent(c -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Ya existe un venue con el nombre '" + venue.getNombre() +
                                    "' en la ciudad: " + c.getNombre());
                });
        Ciudad ciudad = ciudadRepository.findById(ciudadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la ciudad con ID: " + ciudadId
                ));

        venue.setCiudad(ciudad);

        return saveKafka(venue);
    }

    /*** Modifica un venue existente*/
    @Transactional("transactionManager")
    @Override
    public Venue updateVenue(Venue venue){
        try {
            Venue venueExistente = repository.findById(venue.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Venue no encontrado con ID: " + venue.getId()));

            venueExistente.setNombre(venue.getNombre());
            venueExistente.setUrlMapa(venue.getUrlMapa());
            venueExistente.setEventos(venue.getEventos());
            return saveKafka(venue);
        } catch (EntityNotFoundException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el evento: " + e.getMessage(), e);
        }
    }


    /*** Elimina una ciudad existente*/
    @Override
    @Transactional("transactionManager")
    public void deleteById(Long pId) {
        Venue venue = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningun venue con el id proporcionado"));
        if(venue.getEventos() == null || venue.getEventos().isEmpty()) {
            EntityDeleteEventLong venueDelete = new EntityDeleteEventLong(venue.getId());
            try {
                ProducerRecord<String, Object> record = new ProducerRecord<>(venuesTopic, "Venue-" + venue.getId(), venueDelete);
                record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();
                repository.deleteById(venue.getId());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No puede eliminar un venue que contenga eventos");
        }
    }

    @Transactional("transactionManager")
    public Venue saveKafka(Venue pVenue) {
        Venue venue = this.save(pVenue);

        VenueEvent venueEvent = new VenueEvent(
                venue.getId(),
                venue.getNombre(),
                venue.getUrlMapa(),
                venue.getMapaId(),
                venue.getAforo(),
                venue.getCiudad() != null ? venue.getCiudad().getId() : null
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(venuesTopic, "Venue-" + venueEvent.getId(), venueEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return venue;
    }

    @Override
    public List<Venue> findAllByCiudadId(Long ciudadId){
        return repository.findByCiudadId(ciudadId);
    }

}
