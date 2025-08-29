package com.arquitectura.servicio.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.servicio.service.ServicioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servicio")
public class ServicioController extends CommonController<Servicio, ServicioService>
{
}
