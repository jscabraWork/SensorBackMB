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
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "ordenes")
@ToString(exclude = {"tickets", "transacciones","cliente","tarifa","evento"})
public class Orden extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    // 1: ACEPTADA , 2:RECHAZADA, 3: EN PROCESO, 4: DEVOLUCION, 5: FRAUDE 6: UPGRADE
    protected Integer estado;

    //1: COMPRA ESTANDAR DE TICKETS, 2: ADICIONES, 3:CREAR ALCANCIA, 4 APORTAR A ALCANCIA, 5: TRASPASO DE TICKET, 6: ASIGNACION (cortesia)
    protected Integer tipo;

    protected Double valorOrden;

    protected Long idTRXPasarela;

    protected Double valorSeguro;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value="transaccionOrden_mov")
    protected List<Transaccion> transacciones;

    // se actualice a estado en proceso los tickets asociados a la orden
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "orden_tickets",
        joinColumns = @JoinColumn(name = "orden_id"),
        inverseJoinColumns = @JoinColumn(name = "ticket_id")
    )
    @JsonIgnore
    protected List <Ticket> tickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cliente_id")
    @JsonIgnore
    protected Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonManagedReference(value = "tarifa_ordenes")
    @JoinColumn(name = "tarifa_id")
    protected Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="evento_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    protected Evento evento;

    public Double calcularValorOrden() {
        //Validar que el valorSeguro no sea nulo, si es nulo se asigna 0.0
        double seguro = (valorSeguro != null) ? valorSeguro : 0.0;

        return tarifa.calcularPrecioTotal() * tickets.size() + seguro;
    }

    // Constructor para crear una orden a partir de un evento, cliente y lista de tickets
    //Al crear la orden, se procesan los tickets y se calcula el valor de la orden
    public Orden(Evento evento,Cliente cliente, List<Ticket> tickets, Integer tipo) {
        Localidad localidad = tickets.get(0).getLocalidad();
        this.evento = evento;
        this.cliente = cliente;
        estado = 3; // En proceso por defecto
        tarifa = localidad.getTarifaActiva();

        //Si el tipo es nulo, se obtiene el tipo de orden según la localidad
        this.tipo = (tipo != null) ? tipo : getTipoOrden(localidad);

        //El método setTickets tiene en cuenta asientos de tickets que se agregan individualmente a la orden
        setTickets(tickets);

        //Si la orden es de tipo alcancía, no se calcula el valor de la orden
        this.valorOrden = (this.tipo == 2) ? 0.0 : calcularValorOrden();
        procesarTickets();
    }

    //Obtiene el tipo de orden según el tipo de localidad
    @JsonIgnore
    public Integer getTipoOrden(Localidad localidad) {
        switch (localidad.getTipo()) {
            case 0:
                return 1; // Orden de compra estandar
            case 1:
                return 3; // localidad alcancía
            default:
                return 1; // por defecto
        }
    }

    // MANEJO DE TICKETS

    //Agrega todos los tickets a la orden, incluyendo los asientos de cada ticket
    public void setTickets(List<Ticket> tickets) {

        this.tickets = new ArrayList<>();

        tickets.forEach(ticket -> {
            this.tickets.add(ticket);
            if (ticket.getAsientos() != null && ticket.getTipo() == 0) {
                this.tickets.addAll(ticket.getAsientos());
            }
        });
    }

    public void rechazar(){
        estado = 2; // Rechazada

        //SOLO LIBEARAR TICKETS DE LA ORDEN SI ES DE TIPO 1 (COMPRA ESTANDAR) O 3 (CREAR ALCANCIA)
        //No se pueden liberar tickets de ordenes de tipo 4 (aportes a alcancía), 5 (traspaso) o 6 (asignación)
        if(tipo == 1 || tipo == 3) {
            liberarTickets();
        }

        if(transacciones!= null && !transacciones.isEmpty()) {
            transacciones.forEach(transaccion -> transaccion.rechazar());
        }
    }

    public void confirmar(){
        estado = 1; // aprobada
        venderTickets(cliente, tarifa);
    }

    private void liberarTickets(){
        if(tickets != null && !tickets.isEmpty()) {
            this.tickets.forEach(Ticket::liberar);
        }
    }

    private void procesarTickets(){
        this.tickets.forEach(t->{
            t.procesar();
        });
    }

    private void venderTickets(Cliente cliente, Tarifa tarifa){
        this.tickets.forEach(t->{
            t.vender(cliente, tarifa);
        });
    }

    //Calcula el valor orden no a partir de la tarifa propia, sino de la tarifa activa
    // asociada a la localidad de los tickets de la orden
    public Double calcularIva() {
        if (tickets == null || tickets.isEmpty() || tarifa == null) {
            return 0.0;
        }
        return tickets.size() * tarifa.getIva();
    }

    public String getDescripcion() {
        int cantidadTickets = (tickets != null) ? tickets.size() : 0;
        String nombreLocalidad = (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getLocalidad().getNombre() : "GENERAL";
        String nombreEvento = (evento != null) ? evento.getNombre() : "EVENTO";
        return cantidadTickets + " ticket(s) " + nombreLocalidad + " para el evento " + nombreEvento;
    }

    //Se utiliza para mostrar el nombre del evento en las respuestas
    // de la API sin cargar el objeto completo
    @Transient
    private String eventoNombre;
    public void setEventoNombre() {
        this.eventoNombre = evento.getNombre();
    }

    public void devolver(){
        estado = 4; // Devolución
        liberarTickets();
        transacciones.forEach(transaccion -> transaccion.setStatus(4));
    }

    public void fraude(){
        estado = 5; // Fraude
        liberarTickets();
        transacciones.forEach(transaccion -> transaccion.setStatus(5));
    }

    public void upgrade(){
        estado = 6; // Upgrade
        liberarTickets();
        transacciones.forEach(transaccion -> transaccion.setStatus(6));

    }

}
