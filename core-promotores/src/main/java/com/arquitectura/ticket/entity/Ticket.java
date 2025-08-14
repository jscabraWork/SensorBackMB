package com.arquitectura.ticket.entity;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JoinColumn(name = "promotor_id")
    @JsonBackReference(value = "promotor_ticket")
    private Promotor promotor;

    // Getter para obtener el nombre de la localidad y facilitar el reporte de ventas
    @JsonProperty("localidad")
    public String getLocalidadNombre() {
        return localidad != null ? localidad.getNombre() : null;
    }

}
