package com.arquitectura.puntofisico.entity;

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
@Table(name = "puntosfisicos")
public class PuntoFisico {

    @Id
    private String numeroDocumento;
    private String nombre;
    private String correo;
    private String celular;

    private String tipoDocumento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "eventos_puntosfisicos",
            joinColumns = @JoinColumn(name = "puntofisico_numero_documento"))
    @JsonIgnore
    @JsonManagedReference(value = "puntofisico_evento")
    private List<Evento> eventos;
}
