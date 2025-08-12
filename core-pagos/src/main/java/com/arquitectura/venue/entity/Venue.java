package com.arquitectura.venue.entity;


import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(name="venues")
public class Venue extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    @Column(length = 3000)
    @NotNull
    private String urlMapa;

    //Mapa de asientos asociado al venue
    private Long mapaId;

    private Integer aforo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ciudad ciudad;

    @OneToMany(mappedBy = "venue")
    @JsonIgnore
    private List<Evento> eventos;

}
