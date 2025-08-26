package com.arquitectura.views.resumen_organizador;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "resumen_evento_organizador")
public class ResumenOrganizadorView {

    @Id
    private Long id;
}
