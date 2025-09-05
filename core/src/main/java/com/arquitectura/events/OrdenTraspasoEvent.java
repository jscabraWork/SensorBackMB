package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class OrdenTraspasoEvent extends OrdenEvent implements BaseEvent {

    private String clienteReceptorId;

    private TransaccionEvent transaccion;

    public OrdenTraspasoEvent(Long id, Integer estado, Integer tipo, Long eventoId, Double valorOrden,
                                 Double valorSeguro, List<Long> tikcetsIds, String clienteId, Long tarifaId, String clienteReceptorId) {

        super(id, estado, tipo, eventoId, valorOrden, valorSeguro, tikcetsIds, clienteId, tarifaId);

        this.clienteReceptorId = clienteReceptorId;

    }

}
