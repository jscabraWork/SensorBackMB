package com.arquitectura.ticket.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController extends CommonController<Ticket, TicketService> {



}
