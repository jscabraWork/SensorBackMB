package com.arquitectura.ticket.service;

import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.service.IngresoFactory;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketFactory {

    @Autowired
    private IngresoFactory ingresoFactory;

    private Localidad localidad;
    private String numero;
    private int tipo;
    private int estado;
    
    public Ticket crear() {
        Ticket ticket = new Ticket(localidad, tipo, numero, estado);
        
        // Crear ingresos usando IngresoFactory
        List<Ingreso> ingresos = ingresoFactory.crearIngresosPorTicket(ticket, localidad.getDias());

        ticket.setIngresos(ingresos);
        
        return ticket;
    }
    
    public List<Ticket> setAsientos(Integer personas, Ticket ticketPadre) {
        List<Ticket> asientos = new ArrayList<>();
        
        for (int i = 0; i < personas - 1; i++) {
            Ticket asiento = new Ticket(localidad, tipo, numero, estado);
            
            // Crear ingresos usando IngresoFactory
            List<Ingreso> ingresos = ingresoFactory.crearIngresosPorTicket(asiento, localidad.getDias());
            asiento.setIngresos(ingresos);
            
            // Establecer relaciones
            asiento.setPalco(ticketPadre);
            
            asientos.add(asiento);
        }
        
        return asientos;
    }
    
    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }
    
    public void setNumeroTicket(String numero) {
        this.numero = numero;
    }
    
    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
    
    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    public Localidad getLocalidad() {
        return localidad;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public int getTipo() {
        return tipo;
    }
    
    public int getEstado() {
        return estado;
    }
}
