package com.arquitectura.venue.entity;


import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Table(name="venues")
public class Venue  {

    @Id
    private Long id;

    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ciudad ciudad;

    @OneToMany(mappedBy = "venue")
    @JsonIgnore
    private List<Evento> eventos;

}
