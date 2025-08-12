package com.arquitectura.evento.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.services.DiaService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.tarifa.entity.Tarifa;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
public class EventoController extends CommonController<Evento, EventoService> {

    @Autowired
    private DiaService diaService;

    @Autowired
    private LocalidadService localidadService;

    @Override
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Evento pE, BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        Evento entityDB = service.saveKafka(pE);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityDB);
    }

    /**
     * Obtiene todos los eventos filtrados por estado.
     *
     * @param pEstado Estado por el cual filtrar los eventos (1 = inactivo, 0 = activo, etc.)
     * @return ResponseEntity con la lista de eventos que coinciden con el estado proporcionado
     */
    @GetMapping("/listar/estado")
    public ResponseEntity<?> getAllEventosByEstadoAndTemporadaId(@RequestParam int pEstado) {
        return new ResponseEntity<>(service.findAllByEstado(pEstado), HttpStatus.OK);
    }

    /**
     * Obtiene un evento con todos sus datos.
     *
     * @param eventoId id del evento que se busca editar
     * @return ResponseEntity con los datos de un evento para modificarlos
     */
    @GetMapping("/buscar/editar")
    public ResponseEntity<?> getEventoWithVenueAndCiudad(@RequestParam Long eventoId) {

        Map<String, Object> response = new HashMap<>();

        Evento evento =service.findById(eventoId);

        if(evento==null){
            response.put("mensaje", "No se encontró el evento con ID: " + eventoId);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("evento", evento);
        response.put("organizadores", evento.getOrganizadores());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Actualiza el estado de un evento específico.
     *
     * @param eventoId ID del evento a actualizar
     * @param estado Nuevo estado a asignar (1 = activo, 0 = inactivo, etc.)
     * @return ResponseEntity con el evento actualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/estado/{eventoId}")
    public ResponseEntity<?> updateEstado(@PathVariable Long eventoId,
                                          @RequestParam int estado) {
        Evento eventoActualizado = service.actualizarEstado(eventoId, estado);
        if (eventoActualizado == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "No se encontró el evento con ID: " + eventoId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(eventoActualizado);
    }

    /**
     * Actualiza un evento específico.
     *
     * @param id ID del evento a actualizar
     * @param evento datos del evento actualizado
     * @return ResponseEntity con el evento actualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> updateEvento(@PathVariable Long id, @RequestBody Evento evento) {
        Evento eventoActualizado = service.actualizar(id, evento);
        if(eventoActualizado == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "No se encontró el evento con ID: " + id);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return ResponseEntity.ok(eventoActualizado);
    }

    /**
     * Borra un evento específico.
     *
     * @param eventoId ID del evento a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/borrar/{eventoId}")
    public ResponseEntity<?> borrar(@PathVariable Long eventoId) {
        try {
            service.deleteById(eventoId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    //-----------METODOS PERMITIDOS PARA TODOS LOS USUARIOS--------------

    @GetMapping("/venta/{pEventoId}")
    public ResponseEntity<?> getEventoVenta(@PathVariable Long pEventoId) {

        Map<String, Object> response = new HashMap<>();
        Evento evento = service.findById(pEventoId);
        List<Dia> dias = diaService.findAllByEstadoAndEventoId(1, pEventoId);
        dias.stream().forEach(dia -> dia.setLocalidadesVentas());
        response.put("dias",dias);
        response.put("evento", evento);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/perfil/{pEventoId}")
    public ResponseEntity<?> getEventoPerfil(@PathVariable Long pEventoId) {

        Map<String, Object> response = new HashMap<>();
        Evento evento = service.findById(pEventoId);
        List<Localidad> localidades = localidadService.findByEventoIdAndDiaEstado(pEventoId,1);
        response.put("localidades",localidades);
        response.put("evento", evento);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
