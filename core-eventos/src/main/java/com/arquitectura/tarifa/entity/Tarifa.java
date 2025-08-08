package com.arquitectura.tarifa.entity;


import com.arquitectura.entity.Auditable;
import com.arquitectura.localidad.entity.Localidad;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name="tarifas")
public class Tarifa extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "El precio no puede ser nulo")
    private Double precio;

    @NotNull(message = "El servicio no puede ser nulo")
    private Double servicio;

    @NotNull(message = "El iva no puede ser nulo")
    private Double iva;

    //0: Inactiva | 1 Activa | 2 Soldout | 3. CUPON
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    @JsonBackReference
    private Localidad localidad;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        estado =0;
    }
}
