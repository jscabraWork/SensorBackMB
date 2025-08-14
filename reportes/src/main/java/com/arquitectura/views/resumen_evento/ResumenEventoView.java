package com.arquitectura.views.resumen_evento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "resumen_evento")
@Immutable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenEventoView {

    @Id
    private Long eventoId;  // @Column opcional si nombres coinciden

    private String nombre;  // @Column opcional

    @Column(name = "asistentes")
    private Long asistentes;

    @Column(name = "ingresos")
    private Long ingresos;

    @Column(name = "total_recaudado")
    private BigDecimal totalRecaudado;

    @Column(name = "servicio_recaudado")
    private BigDecimal servicioRecaudado;

    @Column(name = "precio_recaudado")
    private BigDecimal precioRecaudado;

    @Column(name = "iva_recaudado")
    private BigDecimal ivaRecaudado;

    @Column(name = "total_cortesias")
    private Long totalCortesias;

    @Column(name = "asistentes_taquilla")
    private Long asistentesTaquilla;

    @Column(name = "pagos_taquilla")
    private Long pagosTaquilla;

    @Column(name = "pagos_pse")
    private Long pagosPse;

    @Column(name = "pagos_tc")
    private Long pagosTc;

    @Column(name = "total_transacciones")
    private Long totalTransacciones;

    @Column(name = "total_compradores")
    private Long totalCompradores;

    @Column(name = "asistentes_pse")
    private Long asistentesPse;

    @Column(name = "asistentes_tc")
    private Long asistentesTc;

    @Column(name = "tickets_vendidos_hoy")
    private Long ticketsVendidosHoy;

    @Column(name = "total_recaudado_hoy")
    private BigDecimal totalRecaudadoHoy;

    @Column(name = "retefuente")
    private BigDecimal retefuente;

    @Column(name = "reteica")
    private BigDecimal reteica;

    @Column(name = "parafiscal")
    private BigDecimal parafiscal;

    @Column(name = "comision_alltickets")
    private BigDecimal comisionAlltickets;

    @Column(name = "comision_pasarela")
    private BigDecimal comisionPasarela;

    @Column(name = "comision_3ds")
    private BigDecimal comision3ds;

}