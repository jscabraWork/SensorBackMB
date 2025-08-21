package com.arquitectura.organizador.service;

import com.arquitectura.views.detalle_evento.DetalleEventoView;
import com.arquitectura.views.historial_transacciones.HistorialDTO;
import com.arquitectura.views.resumen_evento.ResumenEventoView;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;


public interface ReporteService {
    public ResumenEventoView getResumenByEventoId(Long id);

    List<DetalleEventoView> getDetalleEvento(Long eventoId, Long tarifaId, Long localidadId, Long diaId);

    List<HistorialDTO> getHistorialByEventoAndStatus(Long eventoId, Integer status, LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer tipo, int page, int size);

    public byte[] generarExcelHistorialByEventoAndEstado(Long pEventoId, Integer status);
}
