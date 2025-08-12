package com.arquitectura.evento.entity;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.venue.entity.Venue;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Table(name="eventos")
public class Evento  {

    @Id
    private Long id;

    private String pulep;

    private String artistas;

    private String nombre;

    private LocalDateTime fechaApertura;

    //0 CREADO, 1 OCULTO, 2 VISIBLE, 3 TERMINADO
    private int estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Organizador> organizadores;

    @OneToMany(mappedBy = "evento", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference(value = "evento_dia")
    private List<Dia> dias;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id")
    private Tipo tipo;

    @OneToMany (cascade= CascadeType.REMOVE,mappedBy="evento")
    @JsonManagedReference(value="eventoImagen_mov")
    private List<Imagen> imagenes;

    @ManyToMany(mappedBy = "eventos", fetch = FetchType.LAZY)
    @JsonBackReference(value = "puntofisico_evento")
    private List<PuntoFisico> puntosFisicos;


    @ManyToMany(mappedBy = "eventos", fetch = FetchType.LAZY)
    @JsonBackReference(value = "promotores_evento")
    private List<Promotor> promotores;

}
