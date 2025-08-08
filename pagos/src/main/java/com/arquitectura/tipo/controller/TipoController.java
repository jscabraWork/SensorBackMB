package com.arquitectura.tipo.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.service.TipoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tipos")
public class TipoController extends CommonController<Tipo, TipoService> {



}
