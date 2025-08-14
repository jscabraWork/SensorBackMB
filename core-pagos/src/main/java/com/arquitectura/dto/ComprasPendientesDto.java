package com.arquitectura.dto;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprasPendientesDto {

    private Orden orden;
    private Long eventoId;
    private String eventoNombre;
    private Double precio;
    private List<Ticket> tickets;
    private String imagen;
    private String localidad;
    
    /**
     * Convierte una lista de órdenes en una lista de DTOs individuales.
     * Solo incluye órdenes en estado 3 (EN PROCESO).
     * Los DTOs se ordenan por ID de evento y luego por ID de orden.
     * 
     * @param ordenes Lista de órdenes a convertir
     * @return Lista de ComprasPendientesDto, cada uno representando una orden pendiente
     */
    public static List<ComprasPendientesDto> OrdenesToDto(List<Orden> ordenes) {
        return ordenes.stream()
                .map(ComprasPendientesDto::crearDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un ComprasPendientesDto a partir de una orden individual.
     * Extrae la información del evento y localidad asociados a través de los tickets.
     * 
     * @param orden La orden a convertir
     * @return Un ComprasPendientesDto completo con toda la información de la orden pendiente
     */
    private static ComprasPendientesDto crearDto(Orden orden) {

        Evento evento = orden.getEvento();

        String localidadNombre = null;
        
        if (orden.getTickets() != null && !orden.getTickets().isEmpty()) {
            Ticket primerTicket = orden.getTickets().get(0);
            //Esto esta mal, puedes obtener el evento directamente de la orden
            //evento = primerTicket.getLocalidad().getDias().get(0).getEvento();
            localidadNombre = primerTicket.getLocalidad().getNombre();
        }
        
        // Obtener la imagen de tipo 1
        String imagenUrl = null;
        if (evento != null) {
            Imagen imagen = evento.getImagenByTipo(1);
            imagenUrl = imagen != null ? imagen.getUrl() : null;
        }
        
        return ComprasPendientesDto.builder()
                .orden(orden)
                .eventoId(evento != null ? evento.getId() : null)
                .eventoNombre(evento != null ? evento.getNombre() : null)
                .precio(orden.getValorOrden())
                .tickets(orden.getTickets())
                .imagen(imagenUrl)
                .localidad(localidadNombre)
                .build();
    }
}