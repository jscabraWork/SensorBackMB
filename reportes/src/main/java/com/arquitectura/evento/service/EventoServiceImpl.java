package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.views.detalle_evento.DetalleEventoView;
import com.arquitectura.views.detalle_evento.DetalleEventoViewRepository;
import com.arquitectura.views.resumen_evento.ResumenEventoView;
import com.arquitectura.views.resumen_evento.ResumenEventoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {


    @Autowired
    private ResumenEventoViewRepository vistaResumenEvento;

    @Autowired
    private DetalleEventoViewRepository detalleRepository;

    //Se utiliza para encontrar los eventos no terminados por organizador
    @Override
    public List<Evento> findByOrganizadoresNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer pEstado) {
        return repository.findByOrganizadoresNumeroDocumentoAndEstadoNotOrderByFechaAperturaAsc(numeroDocumento, pEstado);
    }

    //Se utiliza para el historial de eventos por organizador
    @Override
    public List<Evento> findByOrganizadoresNumeroDocumentoAndEstado(String numeroDocumento, Integer pEstado) {
        return repository.findByOrganizadoresNumeroDocumentoAndEstadoOrderByFechaAperturaDesc(numeroDocumento, pEstado);
    }

    @Override
    public ResumenEventoView getResumenByEventoId(Long id) {
        return vistaResumenEvento.findByEventoId(id).orElse(null);
    }

    @Override
    public List<DetalleEventoView> getDetalleEvento(Long eventoId, Long tarifaId, Long localidadId, Long diaId) {
        return detalleRepository.findDetalleFiltrado(eventoId, tarifaId, localidadId, diaId);
    }

}
