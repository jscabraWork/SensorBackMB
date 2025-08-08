package com.arquitectura.seguro.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.seguro.service.SeguroService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seguros")
public class SeguroController extends CommonController<Seguro, SeguroService> {
}
