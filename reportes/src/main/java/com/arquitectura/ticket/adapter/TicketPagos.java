package com.arquitectura.ticket.adapter;

import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPagos {
    private Long id;

    // 0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE
    private Integer estado;

    // 0: TICKET COMPLETO, 1: TICKET MASTER DE PALCOS INDIVIDUALES
    private Integer tipo;

    private String numero;

    private Localidad localidad;

    private List<Ticket> asientosReporte;

    private List<Ingreso> ingresosReporte;

}
