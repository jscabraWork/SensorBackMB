package com.arquitectura.codigo_traspaso.entity;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.entity.Auditable;
import com.arquitectura.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Table(name = "codigo_traspaso")
public class CodigoTraspaso extends  Auditable{

    @Id
    @GeneratedValue
    private Long id;

    //Correo del receptor del traspaso
    private String correoDestino;

    private boolean activo;

    @Column(unique = true)
    private String codigo;

    //Cliente que realiza el traspaso
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public CodigoTraspaso(String correo, Cliente cliente, Ticket ticket) {
        this.correoDestino = correo;
        this.cliente = cliente;
        this.ticket = ticket;
        activo = true;
        codigo = java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    }
}
