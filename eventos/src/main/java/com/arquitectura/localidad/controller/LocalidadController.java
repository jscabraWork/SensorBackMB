package com.arquitectura.localidad.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.services.LocalidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/localidades")
public class LocalidadController extends CommonController<Localidad, LocalidadService> {

    /**
     * Crea una nueva localidad.
     *
     * @param localidad Objeto localidad a crear
     * @param diasIds Lista de IDs de días a asociar
     * @return ResponseEntity con la localidad creada o mensaje de error si ya existe
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ResponseEntity<?>crear(@RequestBody Localidad localidad,
                                 @RequestParam(required = false) List<Long> diasIds,
                                 @RequestParam(defaultValue = "false") boolean forzarCreacion) {
        Localidad nuevaLocalidad = service.crear(localidad, diasIds, forzarCreacion);
        return new ResponseEntity<>(nuevaLocalidad, HttpStatus.CREATED);
    }


    /**
     * Actualiza una localidad en específico.
     *
     * @param id de la localidad a actualizar
     * @param localidad datos de la localidad actualizada
     * @param diasIds Lista de IDs de días a asociar
     * @return ResponseEntity con la localidad actualizada
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> updateLocalidad(@PathVariable Long id,
                                             @RequestBody Localidad localidad,
                                             @RequestParam(required = false) List<Long> diasIds,
                                             @RequestParam(defaultValue = "false") boolean forzarActualizacion) {
        Localidad localidadActualizada = service.actualizar(id, localidad, diasIds, forzarActualizacion);
        if(localidadActualizada == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "No se encontró la localidad con ID: " + id);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return ResponseEntity.ok(localidadActualizada);
    }

    /**
     * Borra una localidad en específico.
     *
     * @param localidadId ID de la localidad a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/borrar/{localidadId}")
    public ResponseEntity<?> borrar(@PathVariable Long localidadId) {
        try {
            service.deleteById(localidadId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dia/{pDiaId}")
    public ResponseEntity<?> findByDiaId(@PathVariable Long pDiaId
    ) {
        List<Localidad> localidades = service.findByDia(pDiaId);
        return ResponseEntity.ok(localidades);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/evento/{pEventoId}")
    public ResponseEntity<?> findByEventoId(@PathVariable Long pEventoId
    ) {
        List<Localidad> localidades = service.findByEventoId(pEventoId);
        return ResponseEntity.ok(localidades);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{pId}")
    public ResponseEntity<?> getLocalidadByIdEdicion(@PathVariable Long pId
    ) {
        Map<String, Object> response = new HashMap<>();
        Localidad localidad = service.findById(pId);
        response.put("localidad", localidad);
        response.put("dias", localidad.getDias());
        return ResponseEntity.ok(response);
    }



}
