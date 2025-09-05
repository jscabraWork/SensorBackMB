package com.arquitectura.orden_traspaso.controller;
import com.arquitectura.controller.CommonController;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.orden_traspaso.service.OrdenTraspasoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordenes_traspaso")
public class OrdenTraspasoController extends CommonController<OrdenTraspaso, OrdenTraspasoService>{



}
