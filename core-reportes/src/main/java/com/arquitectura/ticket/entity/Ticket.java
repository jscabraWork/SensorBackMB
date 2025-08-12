package com.arquitectura.ticket.entity;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets")
public class Ticket  extends Auditable{

    @Id
    private Long id;

    // 0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE
    private Integer estado;

    // 0: TICKET COMPLETO, 1: TICKET MASTER DE PALCOS INDIVIDUALES
    private Integer tipo;

    private String numero;

    @ManyToMany(mappedBy = "tickets")
    @JsonIgnore
    private List<Orden> ordenes;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Servicio> servicios;

    @OneToMany(mappedBy = "palco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> asientos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palco_id")
    @JsonIgnore
    private Ticket palco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    @JsonIgnore
    private Localidad localidad;

    @ManyToMany(mappedBy = "tickets")
    @JsonIgnore
    private List<Alcancia> alcancias;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference(value = "ticket-ingreso")
    @JsonIgnore
    private List<Ingreso> ingresos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seguro_id")
    private Seguro seguro;

    //Atributos para venta desde mapas
    @Transient
    private Integer personasPorTicket;

    @Transient
    private Integer asientosDisponibles;

    //Constructor para crear tickets desde Microsercicio de PAGOS
    public Ticket(Long id, Localidad localidad, Integer tipo, String numero, Integer estado, List<Ingreso> ingresos,
    List<Ticket> asientos) {
        this.id = id;
        this.localidad = localidad;
        this.tipo = tipo;
        this.numero = numero;
        this.estado = estado != null ? estado : 0; // Estado por defecto: DISPONIBLE
        this.ingresos = ingresos != null ? ingresos : new ArrayList<>();
        this.asientos = asientos != null ? asientos : new ArrayList<>();
    }


}
