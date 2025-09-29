package com.arquitectura.localidad.entity;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "localidades")
public class Localidad {

    @Id
    private Long id;

    private String nombre;

    //0: normal, 1: localidad-alcancia
    private Integer tipo;

    private Double aporteMinimo;

    private String descripcion;

    @OneToMany(mappedBy = "localidad", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Tarifa> tarifas;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dias_localidades",
            joinColumns = @JoinColumn(name = "localidad_id"),
            inverseJoinColumns = @JoinColumn(name = "dia_id")
    )
    @JsonIgnore
    private List<Dia> dias;

    @OneToMany(mappedBy = "localidad", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> tickets;

    // Retorna las tarifas en estado 1 (activa) y estado 4 de la localidad
    // Este método serializa el objeto tarifas en el json de Localidad
    @JsonProperty("tarifas")
    public List<Tarifa> tarifas() {
        return tarifas.stream()
                .filter(tarifa -> tarifa.getEstado() == 1 || tarifa.getEstado() == 4)
                .toList();
    }
}
