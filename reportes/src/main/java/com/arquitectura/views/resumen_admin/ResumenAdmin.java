package com.arquitectura.views.resumen_admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@Table(name = "resumen_admin")
public class ResumenAdmin {

    @Id
    private Long id;


}
