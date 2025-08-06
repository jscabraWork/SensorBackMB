package com.arquitectura.puntofisico.services;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
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
public class PuntoFisicoServiceImpl extends CommonServiceImplString<PuntoFisico, PuntoFisicoRepository> implements PuntoFisicoService {

    @Value("${eventos-puntosfisicos.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private EventoService eventoService;

    @Override
    public List<PuntoFisico> findByEvento(Long pEventoid) {
        return repository.findByEventoId(pEventoid);
    }

    @Override
    @Transactional("transactionManager")
    public PuntoFisico asignarEventos(String numeroDocumento, List<Long> pEventosId) {
        PuntoFisico punto = findById(numeroDocumento);
        List<Evento> eventos = eventoService.findAllById(pEventosId);
        punto.setEventos(eventos);

        PuntoFisico puntoFisicoBD = repository.save(punto);

        publicarEventoPuntoFisico(puntoFisicoBD);

        return puntoFisicoBD;
    }

    @Override
    public List<PuntoFisico> findByFiltro(String nombre, String numeroDocumento, String correo) {
        return repository.findByFiltro(nombre, numeroDocumento, correo);
    }

    @Transactional("transactionManager")
    private void publicarEventoPuntoFisico(PuntoFisico puntoFisico) {

        EventoVendedorEvent event = new EventoVendedorEvent(
                puntoFisico.getNumeroDocumento(),
                puntoFisico.getEventos() == null ?
                java.util.Collections.emptyList() :
                puntoFisico.getEventos().stream().map(Evento::getId).collect(Collectors.toList())
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "puntoFisico-vento" + puntoFisico.getNumeroDocumento(), event);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
    }

}
