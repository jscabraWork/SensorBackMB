package com.arquitectura.tarifa.service;

import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TarifaServiceImpl extends CommonServiceImpl<Tarifa, TarifaRepository> implements TarifaService {

    @Value("${tarifas.topic}")
    private String tarifasTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public List<Tarifa> findAllByEstadoAndLocalidadId(int pEstado, Long pId) {
        return repository.findAllByEstadoAndLocalidadId(pEstado, pId);
    }

    public List<Tarifa> findAllByEventoId(Long pId) {
        List<Tarifa> tarifas = repository.findAllByEventoId(pId);
        if(tarifas.isEmpty()) {
            throw new EntityNotFoundException("No se encontraron tarifas para el evento con ID: " + pId);
        }
        return tarifas;
    }

    @Override
    @Transactional("transactionManager")
    public Tarifa actualizarEstado(Long pId, int estado) {
        Tarifa tarifa = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró la tarifa con el id proporcionado"));

        // Solo validamos si se quiere activar (estado = 0)
        if (estado == 1) {
            Localidad localidad = tarifa.getLocalidad();
            if (localidad != null) {
                List<Tarifa> tarifasDeLaLocalidad = localidad.getTarifas();
                for (Tarifa t : tarifasDeLaLocalidad) {
                    if (!t.getId().equals(tarifa.getId()) && t.getEstado() == 1) {
                        throw new RuntimeException("Ya existe una tarifa activa para la localidad: " + localidad.getNombre());
                    }
                }
            }
        }

        tarifa.setEstado(estado);

        return saveKafka(tarifa);
    }

    @Override
    @Transactional("transactionManager")
    public Tarifa actualizar(Long pId, Tarifa tarifa) {
        try {
            Tarifa tarifaExistente =  repository.findById(pId)
                    .orElseThrow(() -> new EntityNotFoundException("Tarifa no encontrada con ID: "+ pId));

            tarifaExistente.setNombre(tarifa.getNombre());
            tarifaExistente.setPrecio(tarifa.getPrecio());
            tarifaExistente.setServicio(tarifa.getServicio());
            tarifaExistente.setIva(tarifa.getIva());

            return saveKafka(tarifaExistente);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la tarifa: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional("transactionManager")
    public void deleteById(Long pId) {
        Tarifa tarifa = repository.findById(pId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningúna tarifa con el id proporcionado"));

        if(tarifa.getLocalidad() == null) {
            EntityDeleteEventLong tarifaDelete = new EntityDeleteEventLong(tarifa.getId());
            try {
                // Envía el evento de eliminación a Kafka de forma sincrónica
                ProducerRecord<String, Object> record = new ProducerRecord<>(tarifasTopic, "Tarifa-" + tarifa.getId(), tarifaDelete);
                record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
                kafkaTemplate.send(record).get();

                // Procede con la eliminación después de publicar en Kafka
                repository.deleteById(tarifa.getId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No se puede eliminar la tarifa porque tiene una localidad asociada");
        }
    }



    @Transactional("transactionManager")
    @Override
    public Tarifa saveKafka(Tarifa pTarifa) {

        Tarifa tarifa = this.save(pTarifa);

        TarifaEvent tarifaEvent = new TarifaEvent(
                tarifa.getId(),
                tarifa.getNombre(),
                tarifa.getPrecio(),
                tarifa.getServicio(),
                tarifa.getIva(),
                tarifa.getEstado(),
                tarifa.getLocalidad().getId()
        );

        try {
            // Envía el evento de creación a Kafka de forma sincronico.
            ProducerRecord<String, Object> record = new ProducerRecord<>(tarifasTopic,"Tarifa-"+tarifaEvent.getId(),tarifaEvent);

            record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());

            kafkaTemplate.send(record).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return tarifa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tieneTicketsAsociados(Long tarifaId) {
        return repository.existsTicketsByTarifaId(tarifaId);
    }

    @Override
    public List<Tarifa> findAllByLocalidadId(Long localidadId) {
        return repository.findAllByLocalidadId(localidadId);
    }

}
