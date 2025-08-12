package com.arquitectura.localidad.entity;


import com.arquitectura.dia.entity.Dia;
import com.arquitectura.entity.Auditable;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name="localidades")
public class Localidad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    //0: normal, 1: localidad-alcancia
    private Integer tipo;

    private Double aporteMinimo;

    @Size(max = 100, message = "La descripción no puede superar los 100 caracteres")
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

    // Retorna la tarifa activa de la localidad, si existe
    // Si no hay tarifa activa, retorna null
    //Si hay más de una tarifa activa, retorna la primera encontrada
    // Este método serializa el objeto tarifaActiva en el json de Localidad
    // Atte: Isaac
    @JsonProperty("tarifa")
    public Tarifa getTarifaActiva() {
        if (tarifas == null || tarifas.isEmpty()) {
            return null;
        }
        return tarifas.stream()
                .filter(tarifa -> tarifa.getEstado() == 1)
                .findFirst()
                .orElse(null);
    }

    public Localidad (String nombre, int tipo, Double aporteMinimo, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.aporteMinimo = aporteMinimo;
        this.descripcion = descripcion;
    }

}
