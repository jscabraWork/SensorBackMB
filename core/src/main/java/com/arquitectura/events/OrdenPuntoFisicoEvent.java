package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPuntoFisicoEvent extends OrdenEvent implements BaseEvent{

    private String puntoFisicoId;

    public OrdenPuntoFisicoEvent(Long id, Integer estado, Integer tipo, Long eventoId, Double valorOrden,
                                   Double valorSeguro, List<Long> tikcetsIds, String clienteId, Long tarifaId, String puntoFisicoId) {

        super(id, estado, tipo, eventoId, valorOrden, valorSeguro, tikcetsIds, clienteId, tarifaId);

        this.puntoFisicoId = puntoFisicoId;

    }

}
