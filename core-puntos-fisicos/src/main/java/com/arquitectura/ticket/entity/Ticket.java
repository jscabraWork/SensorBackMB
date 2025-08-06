package com.arquitectura.ticket.entity;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.tarifa.entity.Tarifa;
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
@Table(name = "tickets")
public class Ticket {

    @Id
    private Long id;

    // 0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE
    private int estado;

    // 0: TICKET COMPLETO, 1: TICKET MASTER DE PALCOS INDIVIDUALES
    private int tipo;

    private String numero;

    @OneToMany(mappedBy = "palco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> asientos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palco_id")
    @JsonIgnore
    private Ticket palco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    @JsonIgnore
    private Localidad localidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puntofisico_id")
    @JsonBackReference(value = "puntofisico_ticket")
    private PuntoFisico puntofisico;


}
