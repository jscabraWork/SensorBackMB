package com.arquitectura.configSeguro.service;

import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.configSeguro.entity.ConfigSeguroRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ConfigSeguroServiceImpl extends CommonServiceImpl<ConfigSeguro, ConfigSeguroRepository> implements ConfigSeguroService {

    public ConfigSeguro getConfigSeguroActivo(){
        return repository.findFirstByEstado(1);
    }

}
