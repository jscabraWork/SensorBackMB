package com.arquitectura.evento.entity;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.venue.entity.Venue;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eventos")
public class Evento {

    @Id
    private Long id;

    private String pulep;

    private String artistas;

    private String nombre;

    private LocalDateTime fechaApertura;

    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue;

    @OneToMany(mappedBy = "evento", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference(value = "evento_dia")
    @JsonIgnore
    private List<Dia> dias;

    @ManyToMany(mappedBy = "eventos", fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonBackReference(value = "promotor_evento")
    private List<Promotor> promotores;

    @OneToMany (cascade= CascadeType.REMOVE,mappedBy="evento")
    @JsonManagedReference(value="eventoImagen_mov")
    private List<Imagen> imagenes;

}
