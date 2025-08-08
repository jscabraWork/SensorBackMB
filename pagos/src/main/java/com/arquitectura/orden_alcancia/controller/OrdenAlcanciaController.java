package com.arquitectura.orden_alcancia.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.orden_alcancia.service.OrdenAlcanciaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordenes_alcancias")
public class OrdenAlcanciaController extends CommonController<OrdenAlcancia, OrdenAlcanciaService> {
}
