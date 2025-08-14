package com.arquitectura.dto;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MisTicketsDto {
    private List<Ticket> tickets;
    private Evento evento;
    private Localidad localidad;
}