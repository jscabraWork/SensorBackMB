package com.arquitectura.venue.entity;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "venues")
public class Venue {

    @Id
    private Long id;

    private String nombre;

    private String urlMapa;

    private int mapaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ciudad ciudad;
}
