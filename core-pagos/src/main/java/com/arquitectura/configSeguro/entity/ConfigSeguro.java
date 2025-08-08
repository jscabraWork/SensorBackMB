package com.arquitectura.configSeguro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "config_seguros")
public class ConfigSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String proveedor;

    private Double valorMaximo;

    private Double porcentaje;

    private Integer estado;

    public double calcularValorSeguro(double valorOrden) {
        return Math.ceil(valorOrden * (porcentaje / 100) / 100) * 100;
    }
}
