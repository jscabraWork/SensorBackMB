package com.arquitectura.configSeguro.controller;

import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.controller.CommonController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configSeguros")
public class ConfigSeguroController extends CommonController<ConfigSeguro, ConfigSeguroService> {
}
