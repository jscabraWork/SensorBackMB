package com.arquitectura.orden.service;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface OrdenService extends CommonService<Orden> {

    /**
     * actualiza el estado de una orden por su ID
     * @param pId El ID de la orden a actualizar
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public Orden actualizarEstado(Long pId, int estado);

    /**
     * agrega un t icket por su ID a una orden
     * @param ordenId El ID de la orden a y idTicket el id del ticket para agregar a la orden
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public Orden agregarTicketAOrden(Long ordenId, Long idTicket);

    /**
     * Elimina un ticket por su ID de una orden
     * @param pIdOrden El ID del ticket a eliminar
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public void deleteTicketFromOrden(Long pIdOrden, Long pIdTicket);

    /**
     * Trae todos las ordenes por el cliente id
     * @param numeroDocumento El ID del cliente
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public List<Orden> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento);

    //----------------Creación de ordenes-------------------
    public Orden crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento, Long pLocalidadId) throws Exception;

    public Orden crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento) throws Exception;

    public Orden crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad , Long pEventoId, String pNumeroDocumento) throws Exception;


    //----------------Métodos para Kafka-------------------
    /**
     * Guarda una orden y publica el evento en Kafka
     * @param pOrden La orden a guardar
     * @return La orden guardada
     */
    public Orden saveKafka(Orden pOrden);

    public Orden confirmar(Orden orden) throws Exception;

    /**
     * Elimina una orden por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la orden a eliminar
     */
    public void deleteById(Long pId);

    public String aplicarCupon(String pCuponId, Long pOrdenId);

    public void rechazar(Orden orden);

    public List<Orden> findByEstado(Integer estado);

    public List<Orden> findAllOrdenesSinConfirmacion();

}
