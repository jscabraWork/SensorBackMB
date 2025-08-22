package com.arquitectura.alcancia.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="alcancias")
@Builder
@JsonIgnoreProperties(value={"handler","hibernateLazyInitializer"})
public class Alcancia extends Auditable {

    @Id
    private Long id;

    private Double precioParcialPagado;

    private Double precioTotal;

    //0:PAGADA, 1: ACTIVA, 2: CANCELADA
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cliente_id")
    private Cliente cliente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonManagedReference(value = "alcancia-tickets")
    private List<Ticket> tickets;

    @Transient
    private String localidad;

    public void setLocalidad(){
        localidad = tickets.get(0).getLocalidad().getNombre();
    }
}
