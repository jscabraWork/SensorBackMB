package com.arquitectura.ingreso.controller;


import com.amazonaws.services.greengrassv2.model.LambdaIsolationMode;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.controller.CommonController;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.service.IngresoService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ingresos")
public class IngresoController //extends CommonController<Ingreso, IngresoService>
{



}
