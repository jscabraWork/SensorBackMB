package com.arquitectura.dia.service;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DiaServiceImpl extends CommonServiceImpl<Dia, DiaRepository> implements DiaService {
}
