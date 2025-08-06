package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ImagenEvent implements BaseEvent{

    private Long id;

    private String nombre;

    private String url;

    //1 Perfil, 2 Banner, 3 QR, 4 Publicidad banner pagina Principal
    private int tipo;

    private Long eventoId;

}
