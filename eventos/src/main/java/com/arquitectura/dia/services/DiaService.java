package com.arquitectura.dia.services;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.services.CommonService;

import java.util.List;

public interface DiaService extends CommonService<Dia> {

    /**
     * Obtiene todos los dias filtrados por estado.
     *
     * @param pEstado Estado por el cual filtrar los dias (1 = inactivo, 0 = activo, etc.)
     * @param pId eventoId por el cual filtrar los dias
     * @return ResponseEntity con la lista de dias que coinciden con el estado proporcionado
     */
    public List<Dia> findAllByEstadoAndEventoId(int pEstado, Long pId);


    /**
     * Obtiene todos los días filtradas por evento.
     *
     * @param pId eventoId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de las tarifas que coinciden con el estado proporcionado
     */
    public List<Dia> findAllByEventoId(Long pId);

    /**
     * Actualiza el estado de un dia específico.
     *
     * @param pId ID del dia al que se le cambia el estado
     * @param estado Nuevo estado a asignar (1 = activo, 0 = inactivo, etc.)
     * @return ResponseEntity con el evento actualizado
     */
    public Dia actualizarEstado(Long pId,  int estado);

    /**
     * Actualiza un evento específico.
     *
     * @param pId ID del dia a actualizar
     * @param dia datos del dia actualizado
     * @return ResponseEntity con el evento actualizado
     */
    public Dia actualizar(Long pId, Dia dia);

    /**
     * Borra un dia específico.
     *
     * @param pId ID del dia a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    public void deleteById(Long pId);

    public Dia saveKafka(Dia pDia);


}
