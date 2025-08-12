package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdenAlcanciaEvent extends OrdenEvent implements BaseEvent{

    private Long alcanciaId;

    // Constructor personalizado que incluye todos los campos del padre más el específico
    public OrdenAlcanciaEvent(Long id, Integer estado, Integer tipo, Long eventoId, Double valorOrden,
                              Double valorSeguro, List<Long> ticketsIds, String clienteId, Long tarifaId, Long alcanciaId) {
        super(id, estado, tipo, eventoId, valorOrden, valorSeguro, ticketsIds, clienteId, tarifaId);
        this.alcanciaId = alcanciaId;
    }

    private AlcanciaEvent alcanciaEvent;

}
