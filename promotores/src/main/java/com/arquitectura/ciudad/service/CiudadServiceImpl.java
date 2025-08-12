package com.arquitectura.ciudad.service;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CiudadServiceImpl extends CommonServiceImpl<Ciudad, CiudadRepository> implements CiudadService {
}
