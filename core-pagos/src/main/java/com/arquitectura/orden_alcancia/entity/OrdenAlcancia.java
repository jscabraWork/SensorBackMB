package com.arquitectura.orden_alcancia.entity;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="ordenes_alcancia")
public class OrdenAlcancia extends Orden {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="alcancia_id")
    @JsonBackReference("alcancia_cliente_mov")
    private Alcancia alcancia;

    //Adapter para convertir una orden en una orden de alcancia
    //Se guarda con el mismo id de la orden original y JPA crea la herencia
    public OrdenAlcancia(Alcancia alcancia, Double pAporte, Localidad localidad) {
        this.alcancia = alcancia;
        cliente = alcancia.getCliente();
        estado = 3;
        tickets = alcancia.getTickets();
        valorOrden = pAporte;
        tarifa = alcancia.getTickets().get(0).getTarifa();
        evento = localidad.getDias().get(0).getEvento();
        tipo =4; // tipo 4 para orden de alcancia
    }

    @Override
    public void confirmar(){
        estado = 1; // aprobada
    }
}
