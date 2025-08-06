package com.arquitectura.temporada.entity;

import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temporada")
public class Temporada {

    @Id
    private Long id;

    private String nombre;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    //1: ACTIVO | 0: INACTIVO
    private int estado;

    @OneToMany(mappedBy = "temporada", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Evento> eventos;

}
