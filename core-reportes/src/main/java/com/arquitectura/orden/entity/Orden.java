package com.arquitectura.orden.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.transaccion.entity.Transaccion;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "ordenes")
public class Orden extends Auditable{

    @Id
    protected Long id;

    // 1: ACEPTADA , 2:RECHAZADA, 3: EN PROCESO, 4: DEVOLUCION, 5: FRAUDE 6: UPGRADE
    protected Integer estado;

    //1: COMPRA ESTANDAR DE TICKETS, 2: ADICIONES, 3:CREAR ALCANCIA, 4 APORTAR A ALCANCIA, 5: TRASPASO DE TICKET, 6: ASIGNACION
    protected Integer tipo;

    protected Double valorOrden;

    protected Double valorSeguro;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value="transaccionOrden_mov")
    protected List<Transaccion> transacciones;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    protected List <Ticket> tickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cliente_id")
    @JsonIgnore
    protected Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonManagedReference(value = "tarifa_ordenes")
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="evento_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    protected Evento evento;

}
