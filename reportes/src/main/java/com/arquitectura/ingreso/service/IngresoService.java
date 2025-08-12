package com.arquitectura.ingreso.service;

import com.arquitectura.services.CommonService;
import com.arquitectura.ingreso.entity.Ingreso;

import java.util.List;

public interface IngresoService extends CommonService<Ingreso> {
    
    List<Ingreso> saveAll(List<Ingreso> ingresos);
    
}