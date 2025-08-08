package com.arquitectura.ciudad.controller;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.services.CiudadService;
import com.arquitectura.controller.CommonController;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ciudades")
public class CiudadController extends CommonController<Ciudad, CiudadService> {

    /*** Crea una ciudad
     * @RequestBody se le pasa la entidad ciudad.
     * @return Una respuesta con la nueva ciudad creada.*/
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ResponseEntity<?> createCity(@RequestBody Ciudad ciudad){
        Map<String,Object> response =new HashMap<>();
        Ciudad newCity = service.crear(ciudad);
        response.put("ciudad", newCity);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*** Actualiza una ciudad
     * @RequestBody se le pasa la entidad ciudad.
     * @return Una respuesta con la informacion actualizada de la ciudad.*/
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{pId}")
    public ResponseEntity<?> updateCity(@PathVariable Long pId,  @RequestBody Ciudad ciudad) {
        Ciudad existingCity = service.actualizar(pId, ciudad);
        if (existingCity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Ciudad no encontrada con ID: " + pId));
        }
        return ResponseEntity.ok(existingCity);
    }

    /*** Elimina una ciudad
     * @PathVariable se le pasa el id de la ciudad.
     * @return Una respuesta con la confirmacion de la eliminacion de la ciudad por id.*/
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/borrar/{ciudadId}")
    public ResponseEntity<?> deleteCity(@PathVariable Long ciudadId) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.deleteById(ciudadId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No puede eliminar una ciudad que contenga venues")) {
                response.put("mensaje", "No se pueden eliminar las ciudades que contienen al menos un venue");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    /**
     * Obtiene todas las ciudades.
     * @return ResponseEntity con la lista de ciudades
     */
    @GetMapping("/listarCiudades")
    public ResponseEntity<?> getAllCiudades() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

}
