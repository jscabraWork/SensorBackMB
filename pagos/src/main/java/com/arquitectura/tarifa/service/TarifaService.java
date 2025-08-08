package com.arquitectura.tarifa.service;

import com.arquitectura.services.CommonService;
import com.arquitectura.tarifa.entity.Tarifa;

import java.util.List;

public interface TarifaService extends CommonService<Tarifa> {

    /**
     * Obtiene todas las tarifas filtradas por estado.
     *
     * @param pEstado Estado por el cual filtrar las tarifas (1 = inactivo, 0 = activo, etc.)
     * @param pId localidadId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de tarifas que coinciden con el estado proporcionado
     */
    public List<Tarifa> findAllByEstadoAndLocalidadId(int pEstado, Long pId);

    /**
     * Obtiene todas las tarifas filtradas por evento.
     *
     * @param pId localidadId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de las tarifas que coinciden con el estado proporcionado
     */
    public List<Tarifa> findAllByEventoId(Long pId);

    /**
     * Actualiza el estado de un dia específico.
     *
     * @param pId ID de la tarifa al que se le cambia el estado
     * @param estado Nuevo estado a asignar (1 = activo, 0 = inactivo, etc.)
     * @return ResponseEntity con el tarifa actualizado
     */
    public Tarifa actualizarEstado(Long pId, int estado);

    /**
     * Actualiza un dia específico.
     *
     * @param pId ID de la tarifa a actualizar
     * @param tarifa datos de la tarifa actualizada
     * @return ResponseEntity con la tarifa actualizada
     */
    public Tarifa actualizar(Long pId, Tarifa tarifa);

    /**
     * Borra una tarifa en específico.
     *
     * @param pId ID de la tarifa a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    public void deleteById(Long pId);

    public Tarifa saveKafka(Tarifa pTarifa);

    /**
     * Valida si una tarifa tiene tickets asociados.
     *
     * @param tarifaId ID de la tarifa
     * @return ResponseEntity con un booleano , true = tiene tickts asociados , false = no tiene tickets asociados
     */
    public boolean tieneTicketsAsociados(Long tarifaId);

    public List<Tarifa> findAllByLocalidadId(Long localidadId);


}
