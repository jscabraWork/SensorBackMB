package com.arquitectura.organizador.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name="organizadores")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Organizador {

    @Id
    private String numeroDocumento;

    private String nombre;

    private String correo;

    private String celular;

    private String tipoDocumento;

    @ManyToMany(mappedBy = "organizadores")
    @JsonIgnoreProperties({"organizadores"})
    private List<Evento> eventos;
}
