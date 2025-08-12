package com.arquitectura.tarifa.entity;


import com.arquitectura.entity.Auditable;
import com.arquitectura.localidad.entity.Localidad;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name="tarifas")
public class Tarifa  {

    @Id
    private Long id;

    private String nombre;

    private Double precio;

    private Double servicio;

    private Double iva;

    //0 es activa 1 inactiva
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    @JsonBackReference
    private Localidad localidad;

}
