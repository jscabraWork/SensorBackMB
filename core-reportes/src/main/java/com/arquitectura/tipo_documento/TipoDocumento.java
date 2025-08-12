package com.arquitectura.tipo_documento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    private Long id;
    private String nombre;


}
