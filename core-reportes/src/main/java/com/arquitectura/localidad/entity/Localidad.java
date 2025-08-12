package com.arquitectura.localidad.entity;


import com.arquitectura.dia.entity.Dia;
import com.arquitectura.entity.Auditable;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name="localidades")
public class Localidad  {

    @Id
    private Long id;

    private String nombre;

    //0: normal, 1: localidad-alcancia
    private Integer tipo;

    private Double aporteMinimo;

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

    // Retorna la tarifa activa de la localidad, si existe
    // Si no hay tarifa activa, retorna null
    //Si hay más de una tarifa activa, retorna la primera encontrada
    // Este método serializa el objeto tarifaActiva en el json de Localidad
    // Atte: Isaac
    public Tarifa getTarifaActiva() {
        return tarifas.stream()
                .filter(tarifa -> tarifa.getEstado() == 1)
                .findFirst()
                .orElse(null);
    }

}
