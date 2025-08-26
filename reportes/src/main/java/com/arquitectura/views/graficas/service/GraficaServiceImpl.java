package com.arquitectura.views.graficas.service;

import com.arquitectura.views.graficas.GraficasViewRepository;
import com.arquitectura.views.graficas.dto.GraficaDonaDTO;
import com.arquitectura.views.graficas.dto.GraficaLineasDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraficaServiceImpl implements GraficaService{

    @Autowired
    private GraficasViewRepository repository;

    //Grafica de Donas para el dinero recaudado por metodo de pago
    @Override
    public List<GraficaDonaDTO> getGraficaDineroRecaudadoByMetodo(Long eventoId, Integer mes, Integer anio) {
        return repository.graficaRecaudadoByMetodo(eventoId, mes, anio);
    }

    //Grafica de Lineas y Puntos para las ventas por periodo (meses o dias)
    @Override
    public List<GraficaLineasDTO> getGraficaLineaVentas(Long eventoId, Integer mes, Integer anio) {
        List<Object[]> rawData = repository.getGraficaLineaVentas(eventoId, mes, anio);
        return GraficaLineasDTO.fromObjectArray(rawData);
    }

    @Override
    public List<GraficaDonaDTO> getGraficaDineroRecaudadoByMetodoAdmin(Long eventoId,  Integer anio, Integer mes, Integer dia) {
        return repository.graficaRecaudadoByMetodoAdmin(
            eventoId, 
            mes != null ? mes : -1, 
            dia != null ? dia : -1, 
            anio != null ? anio : -1
        );
    }

    @Override
    public List<GraficaLineasDTO> getGraficaLineaVentasAdmin(Long eventoId, Integer anio, Integer mes, Integer dia) {
        List<Object[]> rawData = repository.getGraficaLineaVentasAdmin(
            eventoId, 
            mes != null ? mes : -1, 
            dia != null ? dia : -1, 
            anio != null ? anio : -1
        );
        return GraficaLineasDTO.fromObjectArray(rawData);
    }
}
