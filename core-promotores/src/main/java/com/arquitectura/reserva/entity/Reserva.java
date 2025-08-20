package com.arquitectura.reserva.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.promotor.entity.Promotor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="reservas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reserva extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clienteId;

    private boolean activa;

    private int cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="localidad_id")
    @JsonBackReference(value="localidadReserva_mov")
    private Localidad localidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="promotor_id")
    private Promotor promotor;

    @PrePersist
    public void prePersist() {;
        this.activa=true;
        super.prePersist();
    }

    public Reserva(String clienteId, boolean activa, int cantidad, Localidad localidad, Promotor promotor) {
        this.clienteId = clienteId;
        this.activa = activa;
        this.cantidad = cantidad;
        this.localidad = localidad;
        this.promotor = promotor;
    }

}

