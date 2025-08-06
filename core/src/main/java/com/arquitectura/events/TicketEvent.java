package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketEvent implements BaseEvent {

    private Long id;

    private String numero;

    // 0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE
    private int estado;

    // 0: TICKET COMPLETO, 1: TICKET MASTER DE PALCOS INDIVIDUALES
    private int tipo;

    private Long localidadId;

    private Long tarifaId;

    private String clienteNumeroDocumento;

    private Long seguroId;

    private Long palcoId;

}
