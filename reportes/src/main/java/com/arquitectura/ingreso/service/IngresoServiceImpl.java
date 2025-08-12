package com.arquitectura.ingreso.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngresoServiceImpl extends CommonServiceImpl<Ingreso, IngresoRepository> implements IngresoService {

    @Override
    public List<Ingreso> saveAll(List<Ingreso> ingresos) {
        return repository.saveAll(ingresos);
    }
    
}