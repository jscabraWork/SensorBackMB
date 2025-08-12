package com.arquitectura.cliente.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.services.CommonServiceImplString;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImpl extends CommonServiceImplString<Cliente, ClienteRepository> implements ClienteService {
}
