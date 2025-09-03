package com.arquitectura.orden_traspaso.entity;

import com.arquitectura.orden.entity.Orden;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name="ordenes_traspaso")
public class OrdenTraspaso extends Orden {



}
