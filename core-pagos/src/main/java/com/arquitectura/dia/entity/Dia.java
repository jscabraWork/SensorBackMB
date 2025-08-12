package com.arquitectura.dia.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(name="dias")
public class Dia extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    //ACTIVO: 1 , INACTIVO: 0
    private int estado;

    @NotNull(message = "La fecha inicio no puede estar vacía")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha fin no puede estar vacía")
    private LocalDateTime fechaFin;

    @NotBlank(message = "La hora inicio no puede estar vacía")
    private String horaInicio;

    @NotBlank(message = "La hora inicio no puede estar vacía")
    private String horaFin;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "dias")
    @JsonIgnore
    private List<Localidad> localidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    @JsonBackReference(value = "evento_dia")
    private Evento evento;

    //Objeto auxiliar para cargar dias con sus localidades en perfil de evento
    //No se quiere que en cada consulta de un dia se carguen las localidades,
    // por eso se usa esto exclusivamente para faclitar la carga del perfil del evento para ventas
    @JsonProperty("localidades") //Serializar como "localidades" en JSON
    @Transient
    private List<Localidad> localidadesVentas;

    public void setLocalidadesVentas() {
        this.localidadesVentas = localidades;
    }
}
