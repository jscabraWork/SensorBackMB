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
    @Column(name = "evento_id")
    private Long eventoId;

    private String nombre;  // @Column opcional

    private Long asistentes;

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

    private BigDecimal retefuente;

    private BigDecimal reteica;

    private BigDecimal parafiscal;

    @Column(name = "comision_alltickets")
    private BigDecimal comisionAlltickets;

    @Column(name = "comision_pasarela")
    private BigDecimal comisionPasarela;

    @Column(name = "comision_3ds")
    private BigDecimal comision3ds;


    public BigDecimal getTotalComisiones() {
        BigDecimal total = BigDecimal.ZERO;
        if (comisionAlltickets != null) total = total.add(comisionAlltickets);
        if (comisionPasarela != null) total = total.add(comisionPasarela);
        if (comision3ds != null) total = total.add(comision3ds);
        return total;
    }

    public BigDecimal getTotalRetenciones() {
        BigDecimal total = BigDecimal.ZERO;
        if (retefuente != null) total = total.add(retefuente);
        if (reteica != null) total = total.add(reteica);
        if (parafiscal != null) total = total.add(parafiscal);
        return total;
    }

    public Double getPorcentajeOcupacion() {
        if (asistentes == null || asistentes == 0) return 0.0;
        if (ingresos == null) return 0.0;
        return (ingresos.doubleValue() / asistentes.doubleValue()) * 100;
    }


}