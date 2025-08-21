package com.arquitectura.reporte.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ventas_promotor")
public class VentaPromorView {

    @Column(name = "evento_id")
    @Id
    private Long eventoId;

    @Column(name = "documento")
    private String documento;

    @Column(name = "correo")
    private String correo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cantidad_vendida")
    Long cantidadVendida;

    @Column(name = "recaudado")
    private Double recaudado;

    @Column(name = "recaudado_precio")
    private Double recaudadoPrecio;

    @Column(name = "recaudado_servicio")
    private Double recaudadoServicio;

    @Column(name = "recaudado_iva")
    private Double recaudadoIva;

}
