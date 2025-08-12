package com.arquitectura.orden_alcancia.entity;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="ordenes_alcancia")
public class OrdenAlcancia extends Orden {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="alcancia_id")
    @JsonBackReference("alcancia_cliente_mov")
    private Alcancia alcancia;
}
