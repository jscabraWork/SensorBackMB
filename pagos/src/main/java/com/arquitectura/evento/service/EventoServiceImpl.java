package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.services.CommonServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {

    @Value("${eventos.topic}")
    private String eventosTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Evento> findAllByEstado(int pEstado) {
        return repository.findAllByEstado(pEstado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public Evento actualizarEstado(Long pId, int estado) {
        Evento evento = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró un evento con el id proporcionado"));

        evento.setEstado(estado);

        return this.saveKafka(evento);
    }


    @Override
    @Transactional("transactionManager")
    public Evento actualizar(Long pId, Evento evento) {
        try {
            Evento eventoExistente = repository.findById(pId)
                    .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + pId));

            eventoExistente.setArtistas(evento.getArtistas());
            eventoExistente.setNombre(evento.getNombre());
            eventoExistente.setRecomendaciones(evento.getRecomendaciones());
            eventoExistente.setDescripcion(eventoExistente.getDescripcion());
            eventoExistente.setVideo(evento.getVideo());
            eventoExistente.setTipo(evento.getTipo());
            eventoExistente.setVenue(evento.getVenue());
            eventoExistente.setFechaApertura(evento.getFechaApertura());
            eventoExistente.setOrganizadores(evento.getOrganizadores());
            eventoExistente.setPulep(evento.getPulep());

            return this.saveKafka(eventoExistente);
        } catch (EntityNotFoundException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el evento: " + e.getMessage(), e);
        }
    }

    @Override
    public Evento getEventoPorIdAndEstadoIn(Long pId, List<Integer> pEstados) {
        return repository.findByIdAndEstadoIn(pId, pEstados);
    }

    @Transactional("transactionManager")
    @Override
    public Evento saveKafka(Evento pEvento) {
        Evento evento = this.save(pEvento);

        EventoEvent eventoEvent = new EventoEvent(
                evento.getId(),
                evento.getPulep(),
                evento.getArtistas(),
                evento.getNombre(),
                evento.getFechaApertura(),
                evento.getEstado(),
                evento.getTipo() != null ? evento.getTipo().getId() : null,
                evento.getVenue() != null ? evento.getVenue().getId() : null,
                evento.getOrganizadores() != null ? evento.getOrganizadores().stream().map(o -> o.getNumeroDocumento().toString()).toList() : List.of()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(eventosTopic, "Evento-" + eventoEvent.getId(), eventoEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
        return evento;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Evento evento = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún evento con el id proporcionado"));
        if(evento.getDias() == null || evento.getDias().isEmpty()) {
            EntityDeleteEventLong eventoDelete = new EntityDeleteEventLong(evento.getId());
            try {
                ProducerRecord<String, Object> record = new ProducerRecord<>(eventosTopic, "Evento-" + evento.getId(), eventoDelete);
                record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();
                repository.deleteById(evento.getId());
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No se puede eliminar el evento porque tiene días asociados");
        }
    }

    @Override
    public Evento findByLocalidadId(Long localidadId) {
        return repository.findByDiasLocalidadesId(localidadId);
    }
}
