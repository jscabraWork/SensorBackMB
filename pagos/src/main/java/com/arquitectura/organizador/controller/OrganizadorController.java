package com.arquitectura.organizador.controller;

import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.services.OrganizadorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizadores")
public class OrganizadorController extends CommonControllerString<Organizador, OrganizadorService>
{
}
