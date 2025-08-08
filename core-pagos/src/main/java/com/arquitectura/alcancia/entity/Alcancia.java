package com.arquitectura.alcancia.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="alcancias")
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value={"handler","hibernateLazyInitializer"})
public class Alcancia extends Auditable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Double precioParcialPagado;

    private Double precioTotal;

    private boolean activa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cliente_id")
    private Cliente cliente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonManagedReference(value = "alcancia-tickets")
    private List<Ticket> tickets;

    public void devolver() {
        activa=false;
        precioParcialPagado=0.0;
        tickets.forEach(Ticket::liberar);
    }

    public Alcancia (Cliente cliente, List<Ticket> tickets, Double precioTotal, Double precioParcialPagado, Tarifa tarifa) {
        this.cliente = cliente;
        this.tickets = tickets;
        this.precioTotal = precioTotal;
        this.precioParcialPagado = precioParcialPagado;
        this.activa = true;
        reservarTickets(tarifa);
    }


    public void aportar(Double aporte) {
        Double nuevoPrecioParcialPagado = precioParcialPagado + aporte;

        // Si el nuevo precio parcial pagado es mayor o igual al precio total, se cierra la alcancía
        if (nuevoPrecioParcialPagado >= precioTotal) {
            precioParcialPagado = precioTotal;
            activa = false; // La alcancía se cierra al alcanzar el total
        }
        else {
            precioParcialPagado += aporte;
        }
    }

    private void reservarTickets(Tarifa tarifa) {
        tickets.forEach(ticket -> ticket.reservar(tarifa));
    }

}
