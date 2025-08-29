package com.arquitectura.transaccion.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.orden.entity.Orden;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transacciones")
@EqualsAndHashCode(callSuper=false)
public class Transaccion extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    //CONSTRUCTOR PARA TRANSACCIONES DE PUNTO FISICO
    public Transaccion(Double amount, String email, String fullName, String idPersona, Integer metodo, String phone, int status, Orden orden) {
        this.amount = amount;
        this.email = email;
        this.fullName = fullName;
        this.idPersona = idPersona;
        this.idPasarela = "No aplica";
        this.ip = null;
        this.metodo = metodo;
        this.metodoNombre = nombreMetodoPago(metodo);
        this.phone = phone;
        this.status = status;
        this.idBasePasarela = "No aplica";
        this.orden = orden;
    }

    private static int metodoPago(String metodo) {
        if (metodo == null) return 5;
        switch (metodo.toUpperCase()) {
            case "EFECTIVO": return 5;
            case "DATAFONO": return 4;
            case "TRANSFERENCIA": return 6;
            case "TARJETA CREDITO": return 1;
            case "PSE": return 2;
            case "TOKEN TARJETA": return 7;
            default: return 5;
        }
    }

    private static String nombreMetodoPago(int metodo) {
        switch (metodo) {
            case 1: return "TARJETA CREDITO";
            case 2: return "PSE";
            case 3: return "DATAFONO";
            case 4: return "EFECTIVO";
            case 5: return "TRANSFERENCIA";
            case 6: return "TOKEN TARJETA";
            default: return "EFECTIVO";
        }
    }

    public boolean isAprobada() {
        return this.status == 34;
    }

    public boolean isPendiente() {
        return this.status == 35;
    }

    //Si el estado es difente que 34 o 35 entonces es rechazada
    public boolean isRechazada() {
        return !this.isAprobada() && !this.isPendiente();
    }

    public void rechazar() {
        this.status = 36; // Rechazada
    }

}
