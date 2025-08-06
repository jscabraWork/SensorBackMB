package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPromotorEvent extends TicketEvent implements BaseEvent {

    private String promotorId;

    public TicketPromotorEvent(Long id, String numero, int estado, int tipo, Long localidadId, Long tarifaId, String clienteNumeroDocumento, Long seguroId, Long palcoId, String promotorId) {
        super(id, numero, estado, tipo, localidadId, tarifaId, clienteNumeroDocumento, seguroId, palcoId);
        this.promotorId = promotorId;
    }
}
