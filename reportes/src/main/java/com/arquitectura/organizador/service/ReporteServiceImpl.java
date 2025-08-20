package com.arquitectura.organizador.service;

import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.views.detalle_evento.DetalleEventoView;
import com.arquitectura.views.detalle_evento.DetalleEventoViewRepository;
import com.arquitectura.views.historial_transacciones.HistorialDTO;
import com.arquitectura.views.historial_transacciones.HistorialRepository;
import com.arquitectura.views.historial_transacciones.HistorialView;
import com.arquitectura.views.resumen_evento.ResumenEventoView;
import com.arquitectura.views.resumen_evento.ResumenEventoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService{

    @Autowired
    private ResumenEventoViewRepository vistaResumenEvento;

    @Autowired
    private DetalleEventoViewRepository detalleRepository;

    @Autowired
    private HistorialRepository historialRepository;

    @Autowired
    private TicketRepository ticketRepository;


    @Override
    public ResumenEventoView getResumenByEventoId(Long id) {
        return vistaResumenEvento.findByEventoId(id).orElse(null);
    }

    @Override
    public List<DetalleEventoView> getDetalleEvento(Long eventoId, Long tarifaId, Long localidadId, Long diaId) {
        return detalleRepository.findDetalleFiltrado(eventoId, tarifaId, localidadId, diaId);
    }

    @Override
    public List<HistorialDTO> getHistorialByEventoAndStatus(Long eventoId, Integer status, LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer tipo, int page, int size) {

        Page<HistorialView> historial = historialRepository.findByFiltrosOrderByFechaDesc(eventoId, status, tipo, fechaInicio, fechaFin, PageRequest.of(page, size));
        List<HistorialDTO> historialDTO = new ArrayList<>();
        historial.forEach(venta ->{
            HistorialDTO dto = new HistorialDTO();
            dto.setVenta(venta);
            //Solo encontrar los tickets si no es una transacción de tipo 4 (APORTE ALCANCIA)
            if(venta.getTipo()!=4){
                dto.setTickets(ticketRepository.findByOrdenesId(dto.getVenta().getOrdenId()));
            }
            historialDTO.add(dto);
        });
        return historialDTO;
    }
}

