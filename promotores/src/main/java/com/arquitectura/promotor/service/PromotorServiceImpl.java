package com.arquitectura.promotor.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.entity.PromotorRepository;
import com.arquitectura.services.CommonServiceImplString;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotorServiceImpl extends CommonServiceImplString<Promotor, PromotorRepository> implements PromotorService {

    @Value("${eventos-promotores.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EventoService eventoService;

    @Override
    public List<Promotor> findByEvento(Long pEventoid) {
        return repository.findByEventoId(pEventoid);
    }

    @Override
    @Transactional("transactionManager")
    public Promotor asignarEventos(String numeroDocumento, List<Long> pEventosId) {
        Promotor promotor = findById(numeroDocumento);
        List<Evento> eventos = eventoService.findAllById(pEventosId);
        promotor.setEventos(eventos);

        Promotor promotorBD = repository.save(promotor);
        publicarEventoPromotor(promotorBD);
        return promotorBD;
    }

    @Override
    public List<Promotor> findByFiltro(String nombre, String numeroDocumento, String correo) {
        return repository.findByFiltro(nombre, numeroDocumento, correo);
    }

    @Transactional("transactionManager")
    private void publicarEventoPromotor(Promotor promotor) {

        EventoVendedorEvent event = new EventoVendedorEvent(
                promotor.getNumeroDocumento(),
                promotor.getEventos() == null ?
                java.util.Collections.emptyList() :
                promotor.getEventos().stream().map(Evento::getId).collect(Collectors.toList())
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "promotor-vento" + promotor.getNumeroDocumento(), event);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
    }
}
