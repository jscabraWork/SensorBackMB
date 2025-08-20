package com.arquitectura.views.historial_transacciones;

import com.arquitectura.ticket.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistorialDTO {
    private HistorialView venta;
    private List<Ticket> tickets;
}

