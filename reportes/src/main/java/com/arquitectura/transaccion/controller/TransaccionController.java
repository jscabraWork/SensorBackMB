package com.arquitectura.transaccion.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaccion")
public class TransaccionController extends CommonController<Transaccion, TransaccionService> {
}
