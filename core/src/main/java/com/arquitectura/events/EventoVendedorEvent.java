package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//Esta clase se utiliza para relacionar los eventos con promotores y puntos fisicos
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventoVendedorEvent implements BaseEvent{

    //Numero de documento del punto fisico o promotor
    private String numeroDocumento;

    //Lista de eventos asociados a este vendedor
    //Si es vacia se eliminaran todos los eventos asociados a este vendedor
    private List<Long> eventosId;

}
