package com.arquitectura.dia.entity;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dias")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Dia {

    @Id
    private Long id;

    private String nombre;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private String horaInicio;

    private String horaFin;

    //ACTIVO: 1 , INACTIVO: 0
    private Integer estado;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "dias")
    private List<Localidad> localidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    @JsonBackReference(value = "evento_dia")
    private Evento evento;

}
