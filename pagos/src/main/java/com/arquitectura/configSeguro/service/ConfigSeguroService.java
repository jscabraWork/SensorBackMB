package com.arquitectura.configSeguro.service;

import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.services.CommonService;

public interface ConfigSeguroService extends CommonService<ConfigSeguro> {

    ConfigSeguro getConfigSeguroActivo();

}
