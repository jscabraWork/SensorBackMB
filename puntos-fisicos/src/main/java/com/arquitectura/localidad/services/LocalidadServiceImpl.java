package com.arquitectura.localidad.services;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LocalidadServiceImpl extends CommonServiceImpl<Localidad, LocalidadRepository> implements LocalidadService {
}
