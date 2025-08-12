package com.arquitectura.orden.service;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrdenServiceImpl extends CommonServiceImpl<Orden, OrdenRepository> implements OrdenService {
}
