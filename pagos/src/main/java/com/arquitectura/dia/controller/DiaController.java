package com.arquitectura.dia.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.service.DiaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dias")
public class DiaController extends CommonController<Dia, DiaService> {


    @Override
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Dia pE, BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        Dia entityDB = service.saveKafka(pE);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityDB);
    }


    /**
     * Obtiene todos los dias filtrados por estado.
     *
     * @param pEstado Estado por el cual filtrar los dias (1 = inactivo, 0 = activo, etc.)
     * @return ResponseEntity con la lista de dias que coinciden con el estado proporcionado
     */
    @GetMapping("/listar/estado")
    public ResponseEntity<?> getAllByEstadoAndEventoId(@RequestParam int pEstado,
                                                       @RequestParam Long eventoId) {
        return new ResponseEntity<>(service.findAllByEstadoAndEventoId(pEstado, eventoId), HttpStatus.OK);
    }

    /**
     * Obtiene todos los días filtradas por evento.
     *
     * @param eventoId eventoId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de las tarifas que coinciden con el estado proporcionado
     */
    @GetMapping("/listar/{eventoId}")
    public ResponseEntity<?> getAllByEventoId(@PathVariable Long eventoId) {
        try {
            return new ResponseEntity<>(service.findAllByEventoId(eventoId), HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }

    /**
     * Actualiza el estado de un dia específico.
     *
     * @param diaId ID del dia al que se le cambia el estado
     * @return ResponseEntity con el dia actualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("estado/{diaId}")
    public ResponseEntity<?> updateDia(@PathVariable Long diaId,
                                       @RequestParam int estado) {
        Dia diaActualizado = service.actualizarEstado(diaId, estado);
        if(diaActualizado == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Día no encontrado con ID: " + diaId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(diaActualizado);
    }

    /**
     * Actualiza un dia específico.
     *
     * @param pId ID del dia a actualizar
     * @param dia datos del dia actualizado
     * @return ResponseEntity con el dia actualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{pId}")
    public ResponseEntity<?> updateDia(@PathVariable Long pId, @RequestBody Dia dia) {
        Dia diaActualizado = service.actualizar(pId, dia);
        if(diaActualizado == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Día no encontrado con ID: " + pId);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return ResponseEntity.ok(diaActualizado);
    }


    /**
     * Borra un dia específico.
     *
     * @param diaId ID del dia a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/borrar/{diaId}")
    public ResponseEntity<?> borrar(@PathVariable Long diaId) {
        try {
            service.deleteById(diaId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }
}
