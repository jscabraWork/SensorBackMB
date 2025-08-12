package com.arquitectura.localidad.service;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tarifa.entity.TarifaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocalidadServiceImpl extends CommonServiceImpl<Localidad, LocalidadRepository> implements LocalidadService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private DiaRepository diaRepository;

    @Value("${localidades.topic}")
    private String localidadesTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@inheritDoc}
     */
    @Transactional("transactionManager")
    @Override
    public Localidad actualizar(Long pId, Localidad localidad, List<Long> diasIds, boolean forzarActualizacion) {
        try {
            Localidad localidadExistente = repository.findById(pId)
                    .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada con ID: " + pId));

            // Verificar si existe otra localidad con el mismo nombre (distinta a la que se está actualizando)
            List<Localidad> localidadesConMismoNombre = repository.findAllByNombreIgnoreCaseAndIdNot(localidad.getNombre(), pId);
            if (!localidadesConMismoNombre.isEmpty() && !forzarActualizacion) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Existen una o mas localidades con el nombre: " + localidad.getNombre()
                );
            }
            // Actualiza atributos simples
            localidadExistente.setNombre(localidad.getNombre());
            localidadExistente.setDescripcion(localidad.getDescripcion());
            localidadExistente.setTipo(localidad.getTipo());
            localidadExistente.setAporteMinimo(localidad.getAporteMinimo());

            // Actualiza días usando los IDs recibidos
            if (diasIds != null && !diasIds.isEmpty()) {
                List<Dia> dias = diaRepository.findAllById(diasIds);
                localidadExistente.setDias(dias);
            } else {
                localidadExistente.setDias(new ArrayList<>());
            }

            return this.saveKafka(localidadExistente);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la localidad: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public Localidad crear(Localidad localidad, List<Long> diasIds, boolean forzarCreacion) {
        List<Localidad> localidadesExistentes = repository.findAllByNombreIgnoreCase(localidad.getNombre());

        if (!localidadesExistentes.isEmpty() && !forzarCreacion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una localidad con el nombre: " + localidad.getNombre()
            );
        }

        Localidad localidadNueva = new Localidad(
            localidad.getNombre(),
            localidad.getTipo() != null ? localidad.getTipo() : 0,
            localidad.getAporteMinimo() != null ? localidad.getAporteMinimo() : 0.0,
            localidad.getDescripcion()
        );
        
        localidadNueva.setDias(new ArrayList<>());

        if (diasIds != null && !diasIds.isEmpty()) {
            List<Dia> dias = diaRepository.findAllById(diasIds);
            localidadNueva.setDias(dias);
        }

        return this.saveKafka(localidadNueva);
    }

    @Transactional("transactionManager")
    @Override
    public Localidad saveKafka(Localidad pLocalidad) {

        Localidad localidad = this.save(pLocalidad);

        LocalidadEvent localidadEvent = new LocalidadEvent(
                localidad.getId(),
                localidad.getNombre(),
                localidad.getTipo(),
                localidad.getAporteMinimo(),
                localidad.getDescripcion(),
                localidad.getDias().stream().map(Dia::getId).toList()
        );
        System.out.println("Enviando evento de creación de localidad: " + localidadEvent);
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(localidadesTopic, "Localidad-" + localidadEvent.getId(), localidadEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
        return localidad;
    }

    @Override
    public List<Localidad> findByDia(Long diaId) {
        return repository.findByDiasId(diaId);
    }

    @Override
    public List<Localidad> findByEventoId(Long pEventoId) {
        return repository.findByDiasEventoId(pEventoId);
    }

    @Override
    public List<Localidad> findByEventoIdAndDiaEstado(Long pEventoId, Integer pEstado) {
        return repository.findByDiasEventoIdAndDiasEstado(pEventoId, pEstado);
    }

    @Transactional("transactionManager")
    public void deleteById(Long pId) {
        Localidad localidad = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna localidad con el id proporcionado"));
        if(localidad.getTarifas() == null || localidad.getTarifas().isEmpty()) {
            EntityDeleteEventLong localidadDelete = new EntityDeleteEventLong(localidad.getId());
            try {
                ProducerRecord<String, Object> record = new ProducerRecord<>(localidadesTopic, "Localidad-" + localidad.getId(), localidadDelete);
                record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();
                repository.deleteById(localidad.getId());
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No se puede eliminar la localidad porque tiene tarifas asociadas");
        }
    }

}
