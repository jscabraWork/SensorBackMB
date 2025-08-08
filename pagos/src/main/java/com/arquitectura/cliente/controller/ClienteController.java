package com.arquitectura.cliente.controller;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.controller.CommonControllerString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
public class ClienteController extends CommonControllerString<Cliente, ClienteService> {

    @GetMapping("/usuario/{pCorreo}")
    public ResponseEntity<?> findByCorreo(@PathVariable String pCorreo) {

        Map<String, Object> response = new HashMap<>();
        response.put("cliente", service.findByCorreo(pCorreo));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/buscar/{pNumeroDocumento}")
    public ResponseEntity<?> findByNumeroDocumento(@PathVariable String pNumeroDocumento) {

        Map<String, Object> response = new HashMap<>();
        response.put("cliente", service.findByNumeroDocumento(pNumeroDocumento));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
