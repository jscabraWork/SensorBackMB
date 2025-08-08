package com.arquitectura.ciudad.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.venue.entity.Venue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(name="ciudades")
public class Ciudad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    @OneToMany(mappedBy = "ciudad", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Venue> venues;
}
