package com.arquitectura.ciudad.entity;

import com.arquitectura.venue.entity.Venue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="ciudades")
public class Ciudad {

    @Id
    private Long id;

    private String nombre;
}
