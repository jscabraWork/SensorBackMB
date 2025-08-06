package com.arquitectura.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdenEvent implements BaseEvent{

    protected Long id;

    protected Integer estado;

    protected Integer tipo;

    protected Long eventoId;

    protected Double valorOrden;

    protected Double valorSeguro;

    protected List<Long> ticketsIds;

    protected String clienteId;

    protected Long tarifaId;
}
