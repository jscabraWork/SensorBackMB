package com.arquitectura.dto;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.imagen.entity.Imagen;
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
public class MisTicketsDto {
    private Ticket ticket;
    private Long eventoId;
    private String eventoNombre;
    private boolean utilizado;
    private String imagen;
    private String localidad;

    /**
     * Convierte una lista de tickets en una lista de DTOs individuales.
     * Cada DTO representa un ticket con la información de su evento y localidad.
     * Los DTOs se ordenan por ID de evento y luego por ID de ticket.
     * 
     * @param tickets Lista de tickets a convertir
     * @return Lista de MisTicketsDto, cada uno representando un ticket individual
     */
    public static List<MisTicketsDto> TicketsToDto(List<Ticket> tickets) {
        return tickets.stream()
                .map(MisTicketsDto::crearDtos)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un MisTicketsDto a partir de un ticket individual.
     * Extrae la información del evento y localidad asociados al ticket.
     * 
     * @param ticket El ticket a convertir
     * @return Un MisTicketsDto completo con toda la información del ticket, evento y localidad
     */
    private static MisTicketsDto crearDtos(Ticket ticket) {
        Evento evento = ticket.getLocalidad().getDias().get(0).getEvento();

        // Obtener la imagen de tipo 1 del evento
        Imagen imagenPrincipal = evento.getImagenByTipo(1);
        String imagenUrl = imagenPrincipal != null ? imagenPrincipal.getUrl() : null;
        
        return MisTicketsDto.builder()
                .ticket(ticket)
                .eventoId(evento.getId())
                .eventoNombre(evento.getNombre())
                .imagen(imagenUrl)
                .utilizado(ticket.isUtilizado())
                .localidad(ticket.getLocalidad().getNombre())
                .build();
    }
}