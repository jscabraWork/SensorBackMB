package com.arquitectura.views.graficas.service;

import com.arquitectura.views.graficas.dto.GraficaDonaDTO;
import com.arquitectura.views.graficas.dto.GraficaLineasDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GraficaService {

    List<GraficaDonaDTO> getGraficaDineroRecaudadoByMetodo(Long eventoId,Integer mes,Integer anio);

    List<GraficaLineasDTO> getGraficaLineaVentas(Long eventoId,Integer mes,Integer anio);

    List<GraficaDonaDTO> getGraficaDineroRecaudadoByMetodoAdmin(Long eventoId,Integer mes,Integer anio);

    List<GraficaLineasDTO> getGraficaLineaVentasAdmin(Long eventoId,Integer mes,Integer anio);
}
