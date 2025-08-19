package com.arquitectura.views.historial_transacciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Entity para la vista historial_transacciones
 * Representa el historial completo de transacciones por evento
 */
@Entity
@Table(name = "historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class HistorialView {

    @Id
    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "evento_id")
    private Long eventoId;

    @Column(name = "tarifa")
    private String tarifa;

    @Column(name = "dia")
    private String dia;

    @Column(name = "localidad")
    private String localidad;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "valor_orden")
    private Double valorOrden;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "tipo")
    private Integer tipo;

    @Column(name = "tipo_nombre")
    private String tipoNombre;

    @Column(name = "metodo")
    private String metodo;

    @Column(name = "correo")
    private String correo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "documento")
    private String documento;

    @Column(name = "estado")
    private Integer estado;

    @Column(name = "status")
    private Integer status;

    @Column(name = "promotor")
    private String promotor;

    @Column(name = "promotor_numero_documento")
    private String promotorNumeroDocumento;

    @Column(name = "tarifa_id")
    private Long tarifaId;

    @Column(name = "localidad_id")
    private Long localidadId;

    @Column(name = "dia_id")
    private Long diaId;

    @Column(name = "cantidad")
    private Long cantidad;
}
