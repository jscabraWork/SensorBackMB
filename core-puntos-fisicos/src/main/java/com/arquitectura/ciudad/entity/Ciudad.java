package com.arquitectura.ciudad.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ciudades")
public class Ciudad {

    @Id
    private Long id;

    private String nombre;
}
