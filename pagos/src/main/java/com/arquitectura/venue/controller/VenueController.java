package com.arquitectura.venue.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/venues")
public class VenueController extends CommonController<Venue, VenueService> {

    /*** Crea un venue
     * @RequestBody se le pasa la entidad venue Y EL Id de la ciudad.
     * @return Una respuesta con el nuevo venue creada.*/
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear/{ciudadId}")
    public ResponseEntity<?> createVenue(@PathVariable Long ciudadId, @RequestBody Venue venue) {
        Map<String, Object> response = new HashMap<>();
        try {
            Venue newVenue = service.createVenue(ciudadId, venue);
            response.put("venue", newVenue);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            response.put("error", e.getReason());
            return new ResponseEntity<>(response, e.getStatusCode());
        }
    }

    /*** Actualiza un venue
     * @RequestBody se le pasa la entidad venue.
     * @return Una respuesta con la informacion actualizada del venue.*/
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{ciudadId}")
    public ResponseEntity<?> updateVenue(@PathVariable Long ciudadId,  @RequestBody Venue venue) {
        Venue existingVenue = service.updateVenue(venue);
        if(existingVenue == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "No se encontró el venue con ID: " + ciudadId);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return ResponseEntity.ok(existingVenue);
    }

    /*** Elimina un venue
     * @PathVariable se le pasa el id del venue.
     * @return Una respuesta con la confirmacion de la eliminacion del venue por id.*/
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/venue/{venueId}")
    public ResponseEntity<?> deleteVenue(@PathVariable Long venueId){
        try{
            service.deleteById(venueId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    /**
     * Obtiene todos los venues.
     * @return ResponseEntity con la lista de venues
     */
    @GetMapping("/listarVenues/{ciudadId}")
    public ResponseEntity<?> getAllVenuesByCiudadId(@PathVariable Long ciudadId) {
        return new ResponseEntity<>(service.findAllByCiudadId(ciudadId), HttpStatus.OK);
    }


}
