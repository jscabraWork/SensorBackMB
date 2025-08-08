package com.arquitectura.localidad.services;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.services.CommonService;

import java.util.List;

public interface LocalidadService extends CommonService<Localidad> {

    /**
     * Actualiza una localidad en específico.
     *
     * @param pId de la localidad a actualizar
     * @param localidad datos de la localidad actualizada
     * @param diasIds Lista de IDs de días a asociar
     * @return ResponseEntity con la localidad actualizada
     */
    public Localidad actualizar(Long pId, Localidad localidad, List<Long> diasIds, boolean forzarActualizacion);

    /**
     * Crea una nueva localidad si no existe otra con el mismo nombre.
     *
     * @param localidad Objeto localidad a crear
     * @param diasIds Lista de IDs de días a asociar
     * @return ResponseEntity con la localidad creada o mensaje de error si ya existe
     */
    public Localidad crear(Localidad localidad, List<Long> diasIds, boolean forzarCreacion);

    /**
     * Borra una localidad en específico.
     *
     * @param pId ID de la localidad a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    public void deleteById(Long pId);

    public Localidad saveKafka(Localidad pLocalidad);

    List<Localidad> findByDia(Long diaId);

    List<Localidad> findByEventoId(Long pEventoId);

    List<Localidad> findByEventoIdAndDiaEstado(Long pEventoId, Integer pEstado);


}
