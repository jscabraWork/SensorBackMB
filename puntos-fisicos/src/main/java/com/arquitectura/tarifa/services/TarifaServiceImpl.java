package com.arquitectura.tarifa.services;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import org.springframework.stereotype.Service;

@Service
public class TarifaServiceImpl extends CommonServiceImpl<Tarifa, TarifaRepository> implements TarifaService {
}
