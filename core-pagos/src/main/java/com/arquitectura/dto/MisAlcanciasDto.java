package com.arquitectura.dto;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.localidad.entity.Localidad;
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
public class MisAlcanciasDto {

    private Alcancia alcancia;
    private Double precioParcialPagado; //Eliminar: La alcancia ya tiene su precio parcial pagado
    private Double precioTotal; //Eliminar: La alcancia ya tiene su precio total calculado
    private List<Ticket> tickets;
    private Long eventoId;
    private String eventoNombre;
    private String imagen;
    private String localidad;
    private Double aporteMinimo;
    
    /**
     * Convierte una lista de alcancías en una lista de DTOs individuales.
     * Solo incluye alcancías activas.
     * Los DTOs se ordenan por ID de evento y luego por ID de alcancía.
     * 
     * @param alcancias Lista de alcancías a convertir
     * @return Lista de MisAlcanciasDto, cada uno representando una alcancía activa
     */
    public static List<MisAlcanciasDto> AlcanciastoDto(List<Alcancia> alcancias) {
        return alcancias.stream()
                .filter(alcancia -> alcancia.isActiva()) // Solo alcancías activas
                .map(MisAlcanciasDto::crearDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un MisAlcanciasDto a partir de una alcancía individual.
     * Extrae la información del evento asociado a través de los tickets.
     * 
     * @param alcancia La alcancía a convertir
     * @return Un MisAlcanciasDto completo con toda la información de la alcancía y evento
     */
    private static MisAlcanciasDto crearDto(Alcancia alcancia) {
        // Obtener el evento a través del primer ticket
        Evento evento = null;
        if (alcancia.getTickets() != null && !alcancia.getTickets().isEmpty()) {
            Ticket primerTicket = alcancia.getTickets().get(0);
            evento = primerTicket.getLocalidad().getDias().get(0).getEvento();
        }
        
        // Obtener la imagen de tipo 1 del evento
        String imagenUrl = null;
        if (evento != null) {
            Imagen imagenPrincipal = evento.getImagenByTipo(1);
            imagenUrl = imagenPrincipal != null ? imagenPrincipal.getUrl() : null;
        }
        
        // Esto lo estas haciendo dos veces, arriba ya obtuviste la localidad del primer ticket y aca lo vuelves a hacer
        Localidad localidad = null;
        if (alcancia.getTickets() != null && !alcancia.getTickets().isEmpty()) {
            localidad = alcancia.getTickets().get(0).getLocalidad();
        }
        
        return MisAlcanciasDto.builder()
                .alcancia(alcancia)
                .precioParcialPagado(alcancia.getPrecioParcialPagado()) // Eliminar: La alcancia ya tiene su precio parcial pagado
                .precioTotal(alcancia.getPrecioTotal()) // Eliminar: La alcancia ya tiene su precio total calculado
                .tickets(alcancia.getTickets())
                .eventoId(evento != null ? evento.getId() : null)
                .eventoNombre(evento != null ? evento.getNombre() : null)
                .imagen(imagenUrl)
                .localidad(localidad.getNombre())
                .aporteMinimo(localidad.getAporteMinimo())
                .build();
    }
}