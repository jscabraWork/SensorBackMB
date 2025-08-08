package com.arquitectura.seguro.entity;

import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "seguros")
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valor;

    private boolean reclamado;

    @ManyToOne
    @JoinColumn(name = "configSeguro_id")
    @JsonIgnore
    private ConfigSeguro configSeguro;

}
