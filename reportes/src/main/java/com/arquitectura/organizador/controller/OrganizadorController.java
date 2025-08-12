package com.arquitectura.organizador.controller;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.service.OrganizadorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizador")
public class OrganizadorController extends CommonControllerString<Organizador, OrganizadorService> {
}