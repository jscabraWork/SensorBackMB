package com.arquitectura.evento.services;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.services.CommonService;

import java.util.List;
import java.util.Map;

public interface EventoService extends CommonService<Evento> {

    /**
     * Obtiene todos los eventos filtrados por estado.
     *
     * @param pEstado Estado por el cual filtrar los eventos (1 = inactivo, 0 = activo, etc.)
     * @return ResponseEntity con la lista de eventos que coinciden con el estado proporcionado
     */
    public List<Evento> findAllByEstado(int pEstado);


    /**
     * Actualiza el estado de un evento específico.
     *
     * @param pId ID del evento a actualizar
     * @param estado Nuevo estado a asignar (1 = activo, 0 = inactivo, etc.)
     * @return ResponseEntity con el evento actualizado
     */
    public Evento actualizarEstado(Long pId, int estado);

    /**
     * Actualiza un evento específico.
     *
     * @param pId ID del evento a actualizar
     * @param evento datos del evento actualizado
     * @return ResponseEntity con el evento actualizado
     */
    public Evento actualizar(Long pId, Evento evento);

    /**
     * Borra un evento específico.
     *
     * @param pId ID del evento a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    public void deleteById(Long pId);

    public Evento saveKafka(Evento pEvento);

    /**
     * Trae un evento específico.
     *
     * @param pId ID del evento a traer y con estados
     * @return ResponseEntity con el evento
     */
    public Evento getEventoPorIdAndEstadoIn(Long pId, List<Integer> pEstados);
}
