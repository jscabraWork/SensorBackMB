package com.arquitectura.organizador.entity;

import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="organizadores")
public class Organizador {

    @Id
    private String numeroDocumento;

    private String nombre;

    private String correo;

    private String celular;

    private String tipoDocumento;

    @ManyToMany
    @JsonBackReference(value = "evento_organizador")
    @JsonIgnore
    private List<Evento> eventos;
}
