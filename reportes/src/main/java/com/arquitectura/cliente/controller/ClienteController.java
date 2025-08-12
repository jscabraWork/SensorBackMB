package com.arquitectura.cliente.controller;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.controller.CommonControllerString;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cliente")
public class ClienteController extends CommonControllerString<Cliente, ClienteService> {
}
