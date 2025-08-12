package com.arquitectura.transaccion.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransaccionServiceImpl extends CommonServiceImpl<Transaccion, TransaccionRepository> implements TransaccionService {
}
