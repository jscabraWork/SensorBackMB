package com.arquitectura.cupon.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.tarifa.entity.Tarifa;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cupones")
public class Cupon extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    private String codigo;

    private LocalDateTime vigencia;

    // Cantidad maxima de tickets que se pueden comprar con este cupon
    // Cuando se ha vendido la cantidad maxima de cupones, este ya no es aplicable y se inactiva
    private Integer ventaMaxima;

    // el cupon es aplicable solo si la cantidad de tickets a comprar es mayor
    // o igual al minimo de boletas
    private Integer cantidadMinima;

    // el cupon es aplicable solo si la cantidad de tickets a comprar es menor
    // o igual al maximo de boletas
    private Integer cantidadMaxima;

    //0 inactivo, 1 activo
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    @JsonIgnoreProperties(value={"handler","hibernateLazyInitializer","cupones"})
    private Tarifa tarifa;

    private boolean isVigente() {
        //Si el cupon no tiene vigencia, se ignora este criterio
        if(vigencia == null) {
            return true;
        }
        //Es vigente si la vigencia es mayor a la fecha actual
        return vigencia.isAfter(LocalDateTime.now());
    }

    private boolean isActivo() {
        if(estado == null) {
            return false;
        }
        return estado == 1;
    }

    public boolean cumpleCantidadMinima(Integer cantidadTickets) {
        return cantidadMinima == null || cantidadMinima == 0 || cantidadTickets >= cantidadMinima;
    }

    public boolean cumpleCantidadMaxima(Integer cantidadTickets) {
        return cantidadMaxima == null || cantidadMaxima == 0 || cantidadTickets <= cantidadMaxima;
    }

    public boolean cumpleVentaMaxima(Integer ventasActuales) {
        return ventaMaxima == null || ventaMaxima == 0 || ventasActuales < ventaMaxima;
    }

    public boolean isValido(Integer cantidadTickets, Integer ventasActuales) {
        return isActivo() && isVigente() &&
               cumpleCantidadMinima(cantidadTickets) && 
               cumpleCantidadMaxima(cantidadTickets) && 
               cumpleVentaMaxima(ventasActuales);
    }
}
