package com.arquitectura.dia.service;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.events.DiaEvent;
import com.arquitectura.events.EntityDeleteEventLong;
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
public class DiaServiceImpl extends CommonServiceImpl<Dia,DiaRepository> implements DiaService {

    @Value("${dias.topic}")
    private String diasTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Dia> findAllByEstadoAndEventoId(int pEstado, Long pId) {
        return repository.findAllByEstadoAndEventoId(pEstado, pId);
    }

    /**
     * {@inheritDoc}
     */
    public List<Dia> findAllByEventoId(Long pId) {
        List<Dia> dias = repository.findAllByEventoId(pId);
        if (dias.isEmpty()) {
            throw new EntityNotFoundException("No se encontraron días para el evento con ID: " + pId);
        }
        return dias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public Dia actualizarEstado(Long pId, int estado) {
        Dia dia = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró un día con el id proporcionado"));

        dia.setEstado(estado);
        this.saveKafka(dia);
        return dia;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public Dia actualizar(Long pId, Dia dia) {
        try {
            Dia diaExistente = repository.findById(pId)
                    .orElseThrow(() -> new EntityNotFoundException("Día no encontrado con ID: "+ pId));

            diaExistente.setNombre(dia.getNombre());
            diaExistente.setFechaInicio(dia.getFechaInicio());
            diaExistente.setFechaFin(dia.getFechaFin());
            diaExistente.setHoraInicio(dia.getHoraInicio());
            diaExistente.setHoraFin(dia.getHoraFin());

            return this.saveKafka(diaExistente);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el día: " + e.getMessage(), e);
        }
    }


    @Transactional("transactionManager")
    @Override
    public Dia saveKafka(Dia pDia) {

        Dia dia = this.save(pDia);

        DiaEvent diaEvent = new DiaEvent(
                dia.getId(),
                dia.getNombre(),
                dia.getFechaInicio(),
                dia.getFechaFin(),
                dia.getHoraInicio(),
                dia.getHoraFin(),
                dia.getEvento().getId(),
                dia.getEstado()
        );
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(diasTopic, "Dia-" + diaEvent.getId(), diaEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
        return dia;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Dia dia = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún día con el id proporcionado"));
        if(dia.getLocalidades() == null || dia.getLocalidades().isEmpty()) {
            EntityDeleteEventLong diaDelete = new EntityDeleteEventLong(dia.getId());
            try {
                ProducerRecord<String, Object> record = new ProducerRecord<>(diasTopic, "Dia-" + dia.getId(), diaDelete);
                record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();
                repository.deleteById(dia.getId());
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No se puede eliminar el día porque tiene localidades asociadas");
        }
    }


}
