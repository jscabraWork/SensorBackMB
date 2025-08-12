package com.arquitectura.promotor.entity;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "promotores")
public class Promotor {

    @Id
    private String numeroDocumento;
    private String nombre;
    private String correo;
    private String celular;

    private String tipoDocumento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "eventos_promotores", joinColumns = @JoinColumn(name = "promotor_numero_documento"))
    @JsonManagedReference(value = "promotores_evento")
    @JsonIgnore
    private List<Evento> eventos;
}
