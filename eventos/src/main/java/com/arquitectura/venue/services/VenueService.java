package com.arquitectura.venue.services;

import com.arquitectura.services.CommonService;
import com.arquitectura.venue.entity.Venue;

import java.util.List;

public interface VenueService extends CommonService<Venue> {

    /*** Crea un venue se le pasa el id de la ciudad y los datos del venue*/
    public Venue createVenue(Long ciudadId,Venue venue);

    /**
     * Actualiza un venue específico.
     *
     * @param venue datos del venue actualizado
     * @return ResponseEntity con el venue actualizado
     */
    public Venue updateVenue(Venue venue);


    /**
     * Elimina un venue por su ID
     * @param pId El ID del venue a eliminar
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public void deleteById(Long pId);

    /**
     * trae los venues por el id de la ciudad
     * @param ciudadId El ID de la ciudad
     * @return ResponseEntity con todos los venues
     */
    public List<Venue> findAllByCiudadId(Long ciudadId);

    public Venue saveKafka(Venue pVenue);

}
