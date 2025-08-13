package com.arquitectura.evento.entity;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.entity.Auditable;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.venue.entity.Venue;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@ToString(exclude = {"dias","venue"})
@Table(name="eventos")
public class Evento extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El PULEP no puede estar vacío")
    private String pulep;

    @NotBlank(message = "Los artistas no pueden estar vacíos")
    private String artistas;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String recomendaciones;

    private String video;

    private LocalDateTime fechaApertura;

    //0 CREADO, 1 OCULTO, 2 VISIBLE, 3 TERMINADO
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "eventos")
    @JsonManagedReference(value = "evento_organizador")
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

    @PrePersist
    public void prePersist() {
        estado=0; // Por defecto, el estado es CREADO (0)
        super.prePersist();
    }

}
