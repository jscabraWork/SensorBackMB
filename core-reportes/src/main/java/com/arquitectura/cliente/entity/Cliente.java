package com.arquitectura.cliente.entity;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.tipo_documento.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
@JsonIgnoreProperties(value={"handler","hibernateLazyInitializer"})
public class Cliente {

    @Id
    private String numeroDocumento;
    private String nombre;
    private String correo;
    private String celular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoDocumento tipoDocumento;



}
