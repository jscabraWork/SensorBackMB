package com.arquitectura.ticket.entity;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "tickets")
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"ordenes", "servicios", "asientos", "palco", "cliente", "tarifa", "localidad","ingresos"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ticket extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // 0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE
    private int estado;

    // 0: TICKET COMPLETO, 1: TICKET MASTER DE PALCOS INDIVIDUALES
    private int tipo;

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

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonManagedReference(value = "ticket-ingreso")
    @JsonIgnore
    private List<Ingreso> ingresos;

    @ManyToOne
    @JoinColumn(name = "seguro_id")
    private Seguro seguro;

    //-------------------------------------------------------


    // Constructor para TicketFactory
    public Ticket(Localidad localidad, int tipo, String numero, int estado) {
        this.localidad = localidad;
        this.tipo = tipo;
        this.numero = numero;
        this.estado = estado;
    }

    public void procesar(){
        setEstado(3);

    }

    public void reservar(Tarifa tarifa){
        estado =2;
        this.tarifa = tarifa;
    }

    public void liberar(){
        setEstado(0);
        setCliente(null);
        setTarifa(null);
    }

    public void vender(Cliente cliente, Tarifa tarifa) {
        setEstado(1);
        setCliente(cliente);
        setTarifa(tarifa);
    }

    //Método para calcular el precio actual del ticket
    //asociados a partir de la tarifa activa de su localidad
    //Usado para calcular el valor de la orden al crearla
    public Double calcularPrecioActual() {
        // Validaciones de localidad y tarifa activa
        if (localidad == null) {
            return 0.0;
        }
        Tarifa tarifaActiva = localidad.getTarifaActiva();
        if (tarifaActiva == null) {
            return 0.0;
        }
        return tarifaActiva.calcularPrecioTotal();
    }

    //Método recirsivo para calcular el precio de venta del ticket con sus asientos asociados a partir de la tarifa asignada de venta
    public Double getPrecio() {
        if (tarifa == null) {
            return 0.0;
        }
        return tarifa.calcularPrecioTotal();
    }


    public void ve(Cliente cliente, Tarifa tarifa) {
        Cliente lider = asientos.get(0).getPalco().getCliente();
        asientos.forEach(a->{
            if(lider!=null) {
                if(a.getCliente().getNumeroDocumento().equals(lider.getNumeroDocumento())) {
                    a.setEstado(1);
                    a.setCliente(cliente);
                }
            }
            else {
                a.setEstado(1);
                a.setCliente(cliente);
            }
        });
    }

    public boolean isDisponible() {
        return estado == 0;
    }

    public boolean isReservado() {
        return estado == 2;
    }

    public boolean isEnProceso() {
        return estado == 3;
    }

    public boolean isNoDisponible() {
        return estado == 4;
    }

    public boolean isVendido() {
        return estado == 1;
    }

    /**
     * Verifica si todos los ingresos del ticket han sido utilizados
     * @return true si todos los ingresos están utilizados, false si hay alguno sin utilizar o no hay ingresos
     */
    public boolean isUtilizado() {
        if (ingresos == null || ingresos.isEmpty()) {
            return false;
        }
        return ingresos.stream().allMatch(ingreso -> ingreso.isUtilizado());
    }

    @JsonIgnore
    public Evento getEvento() {
        return localidad.getEvento();
    }

    //Atributos para venta desde mapas
    @Transient
    private Integer personasPorTicket;

    @Transient
    private Integer asientosDisponibles;


    //Atributos para creación de tickets en reporte de ventas
    @Transient
    private List<Ingreso> ingresosReporte;
    public void setIngresosReporte() {
        this.ingresosReporte = ingresos;
    }

    //Atributos para creación de tickets en reporte de ventas
    @Transient
    private List<Ticket> asientosReporte;
    public void setAsientosReporte() {
        this.asientosReporte = asientos;
    }

    @JsonProperty("cliente")
    @Transient
    private Cliente clienteT;
    public void setClienteTransient() {
        this.clienteT = cliente;
    }

    //Utilizado para simplificar saber si el ticket esta utilizado en el front
    public boolean getUtilizado() {
        return isUtilizado();
    }
}
