package com.arquitectura.tarifa.entity;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tarifas")
public class Tarifa {

    @Id
    private Long id;

    private String nombre;

    private Double precio;

    private Double servicio;

    private Double iva;

    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    @JsonBackReference(value = "localidad_tarifa")
    @JsonIgnore
    private Localidad localidad;

}
