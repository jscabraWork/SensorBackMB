package com.arquitectura.views.graficas;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

//Esta entidad es solo auxiliar para usar el repository con JPA y no es una entidad completa
@Entity
@Table(name = "grafica_lineas")
@Immutable // Indica que esta entidad es de solo lectura (vista)
public class GraficaView {

    @Id
    @Column(name = "evento_id")
    private Long eventoId;

}
