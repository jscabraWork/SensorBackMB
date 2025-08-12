package com.arquitectura.organizador.service;

import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.entity.OrganizadorRepository;
import com.arquitectura.services.CommonServiceImplString;
import org.springframework.stereotype.Service;

@Service
public class OrganizadorServiceImpl extends CommonServiceImplString<Organizador, OrganizadorRepository> implements OrganizadorService {
}
