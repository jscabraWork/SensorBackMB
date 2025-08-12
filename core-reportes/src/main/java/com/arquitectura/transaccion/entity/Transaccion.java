package com.arquitectura.transaccion.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.orden.entity.Orden;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transacciones")
public class Transaccion extends Auditable{

    @Id
    private Long id;

    private Double amount;
    private String email;
    private String fullName;
    private String idPersona;
    private String idPasarela;
    private String ip;
    //1: TARJETA CREDITO, 2: PSE 3: DATAFONO 4: EFECTIVO 5: TRANSFERENCIA 6: TOKEN TARJETA
    private int metodo;
    private String metodoNombre;
    private String phone;

    // 34: APROBADA, 35: EN PROCESO, 36: RECHAZADA, 4: DEVOLUCION, 5:FRAUDE, 7: ASIGNACION,  8: UPGRADE
    private int status;
    private String idBasePasarela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonIgnore
    private Orden orden;

}

