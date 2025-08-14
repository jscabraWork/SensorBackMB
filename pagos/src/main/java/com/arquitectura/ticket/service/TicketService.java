package com.arquitectura.ticket.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.dto.MisTicketsDto;
import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.transaccion.entity.Transaccion;
import com.google.zxing.WriterException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TicketService extends CommonService<Ticket> {

    /**
     * trae todos los tickets de una orden por su id
     * @param ordenId El ID del ticket a eliminar
     */
    public List<Ticket> getAllByOrdenId(Long ordenId);

    /**
     * Obtiene todos los tickets filtrados por localidad y estado.
     *
     * @param localidadId id de la localidad por el cual filtrar los tickets
     * @param estado Estado por el cual filtrar los tickets (0: DISPONIBLE, 1: VENDIDO, 2: RESERVADO, 3: EN PROCESO, 4: NO DISPONIBLE)
     * @return ResponseEntity con la lista de tickets que coinciden con el estado proporcionado
     */
    Page<Ticket> getAllByLocalidadIdAndEstado(Long localidadId, int estado, int page, int size);

    /**
     * Obtiene todos los tickets hijos de un ticket palco.
     * @param idPalco del ticket de tipo palco
     * @return ResponseEntity con la lista de tickets hijos de un ticket palgo
     */
    public List<Ticket> obtenerHijosDelPalco(Long idPalco);

    /**
     * Obtiene un ticket
     * @param pId del ticket a buscar
     * @param localidadId localidad a la que pertenece el ticket a buscar
     * @param estado estado en el que se encuentra el ticket
     * @return ResponseEntity con el ticket
     */
    public Ticket getByLocalidadAndEstado(Long pId, Long localidadId, int estado);

    /**
     * Crea múltiples tickets en un rango numérico especificado con opciones de personalización.
     *
     * Este metodo permite la creación de tickets en un rango numérico desde 'numeroAbajo' hasta 'numeroArriba',
     * con opciones para configurar si son numerados, si tienen prefijo de letra, y si cada ticket representa
     * un palco con múltiples asientos.
     *
     * @param localidadId localidad que se le asignara al ticket al ser creado
     * @param numeroArriba Número más alto en el rango de tickets a crear (inclusivo)
     * @param numeroAbajo Número más bajo en el rango de tickets a crear (inclusivo)
     * @param letra Prefijo opcional para los tickets numerados (puede ser null, "null", "undefined" o espacio en blanco)
     * @param numerado Indica si se debe asignar numeración secuencial a los tickets (true) o no (false)
     * @param personas Cantidad de asientos por ticket; si es mayor a 1, se crearán tickets "esclavos" asociados al ticket principal
     *
     * @return Map con dos posibles estructuras:
     *         - En caso de éxito: {"mensaje": "Tickets creados exitosamente", "tickets": [lista de tickets creados]}
     *         - En caso de error: {"error": "Descripción del error"}
     *
     * @throws RuntimeException Si hay problemas al recuperar la localidad o guardar los tickets
     */
    public List<Ticket> crearTickets(Long localidadId, Integer numeroArriba, Integer numeroAbajo, String letra, boolean numerado, int personas);

    /**
     * Agrega hijos a un palco
     * @param pIdTicketPadre id del ticket de tipo palco
     * @param pCantidad cantidad de tickets que se agregaran al palco
     * @return ResponseEntity OK indicando que fue un exito
     */
    public ResponseEntity<?> agregarHijos(Long pIdTicketPadre, Integer pCantidad);

    public List<Ticket> findTicketsByLocalidadIdAndEstado(Long idLocalidad, int estado, int cantidad);

    /**
     * Actualiza el estado de un ticket o de muchos tickets.
     *
     * @param pId id de tickets a actualizar
     * @param estado Nuevo estado a asignar
     * @return ResponseEntity con el ticket actualizado
     */
    public Map<String, Object> actualizarEstado(Long pId, int estado, boolean forzar);

    /**
     * Elimina un ticket por su ID, siempre y cuando no tenga órdenes asociadas.
     *
     * @param pId ID del ticket a eliminar
     * @return ResponseEntity sin contenido si se elimina correctamente,
     *         o con un mensaje de error si el ticket tiene órdenes asociadas
     */
    public void eliminarSiNoTieneOrdenes(Long pId);

    /**
     * Actualiza la información de un ticket o muchos tickets.
     *
     * @param ticket entidad actualizada
     * @param forzar confirmacion de usuario
     * @return ResponseEntity con el ticket actualizado
     */
    public Map<String, Object> actualizarTicket(Ticket ticket, boolean forzar);

    /**
     * Asigna un cliente a un ticket.
     *
     * @param pCliente cliente al que se le asignara el ticket
     * @param pIdTicket id del ticket que se asignara
     * @param pToken token para autorizacion de la asignacion
     * @return ResponseEntity 201 con el cliente asignado al ticket
     */
    public String agregarTicketACliente(Long pIdTicket, Cliente pCliente, String pToken) throws Exception;


    /**
     * Busca tickets por su localidad id.
     *
     * @param pLocalidadId id de la localidad
     */
    public List<Ticket> findAllByLocalidad(Long pLocalidadId);


    /**
     * Envia un codigo QR con la informacion del ticket.
     *
     * @param pTicket  el ticket comprado
     */
    public void mandarQR(Ticket pTicket) throws WriterException, IOException, Exception;


    //----------------Métodos para Kafka-------------------
    /**
     * Guarda un ticket y publica el evento en Kafka
     * @param pTicket El ticket a guardar
     * @return El ticket guardado
     */
    public Ticket saveKafka(Ticket pTicket);

    public List<Ticket> saveAllKafka(List<Ticket> pTickets);

    /**
     * Elimina un ticket por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del ticket a eliminar
     */
    public void deleteById(Long pId);

    /**
     * Obtiene los tickets de un cliente organizados por evento
     * @param numeroDocumento Número de documento del cliente
     * @return Lista de MisTicketsDto organizados por evento
     */
    public List<MisTicketsDto> getMisTicketsByCliente(String numeroDocumento);

    public void crearTicketsReporte(List<Ticket> tickets, Long localidadId);

    public void enviar(List<Ticket> tickets) throws Exception;

    public Integer validarVentasCupon(Long pTarifaId);
}
