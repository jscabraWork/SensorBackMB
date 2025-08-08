package com.arquitectura.ingreso.controller;


import com.arquitectura.controller.CommonController;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.service.IngresoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingresos")
public class IngresoController extends CommonController<Ingreso, IngresoService> {
}
