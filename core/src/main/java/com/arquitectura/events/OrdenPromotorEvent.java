package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class OrdenPromotorEvent extends OrdenEvent implements BaseEvent{

    private String promotorId;

    public OrdenPromotorEvent(Long id, Integer estado, Integer tipo, Long eventoId, Double valorOrden,
                                 Double valorSeguro, List<Long> tikcetsIds, String clienteId, Long tarifaId, String promotorId) {

        super(id, estado, tipo, eventoId, valorOrden, valorSeguro, tikcetsIds, clienteId, tarifaId);

        this.promotorId = promotorId;

    }

}
