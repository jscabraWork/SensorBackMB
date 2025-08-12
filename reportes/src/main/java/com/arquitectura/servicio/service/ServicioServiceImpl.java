package com.arquitectura.servicio.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.servicio.entity.ServicioRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicioServiceImpl extends CommonServiceImpl<Servicio, ServicioRepository> implements ServicioService {
}
