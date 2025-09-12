package com.arquitectura.ticket.entity;

import org.springframework.data.domain.Page;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Trae todos los tickets por su orden id
     * @param ordenId El ID de la orden
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    List<Ticket> findByOrdenesId(Long ordenId);

    /**
     * Trae solo los tickets tipo completos y tipo palco (padres) por localidad y estado, de forma paginada
     * @param localidadId ID de la localidad
     * @param estado Estado del ticket
     * @param pageable Objeto de paginación
     * @return Página de tickets palco padres
     */
    @Query("""
    SELECT t FROM Ticket t
    WHERE t.localidad.id = :localidadId
      AND t.estado = :estado
      AND (t.tipo = 0 OR t.tipo = 1)
      AND t.palco IS NULL
""")
    Page<Ticket> findTicketsCompletosYPalcosPadresByLocalidadAndEstado(
            @Param("localidadId") Long localidadId,
            @Param("estado") int estado,
            Pageable pageable
    );

    @Query("""
    SELECT t FROM Ticket t
    WHERE t.id = :ticketId
    AND t.localidad.id = :localidadId
    AND t.estado = :estado
    AND t.tipo = 0
""")
    Optional<Ticket> findTicketCompletoYPalcoPadreByLocalidadAndEstado(
            @Param("ticketId") Long ticketId,
            @Param("localidadId") Long localidadId,
            @Param("estado") int estado
    );

    @Query("SELECT t FROM Ticket t WHERE t.palco.id = :palcoId")
    List<Ticket> findByPalcoId(@Param("palcoId") Long palcoId);

    /**
     * Cuenta la cantidad de personas por palco
     * @param idsPalcos lista de palcos a contar
     * @return Lista de objetos con la cantidad de cada palco
     */
    @Query("SELECT t.id, COUNT(hijo) + 1 FROM Ticket t LEFT JOIN t.asientos hijo WHERE t.id IN :idsPalcos GROUP BY t.id")
    List<Object[]> contarAsientosPorPalco(@Param("idsPalcos") List<Long> idsPalcos);


    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT t FROM Ticket t WHERE t.localidad.id = :idLocalidad AND t.estado = :estado AND t.palco IS NULL")
    List<Ticket> findByLocalidadIdAndEstadoLimitedTo(@Param("idLocalidad") Long idLocalidad, @Param("estado") int estado, Pageable pageable);


    // ----MÉTODOS PARA MAPAS----

    // Trae todos los tickets por localidad
    //ATTE: Isaac, lo uso en el micriservicio de MAPAS
    public List<Ticket> findByLocalidadId(Long pIdLocalidad);

    /**
     * Obtiene todos los tickets padres por localidad ID
     */
    public List<Ticket> findByLocalidadIdAndPalcoIdIsNull(Long pIdLocalidad);

    /**
     * Cuenta la cantidad de asientos disponibles (estado 0) por palco
     * @param idsPalcos lista de palcos a contar
     * @return Lista de objetos con la cantidad de asientos disponibles por palco
     */
    @Query("SELECT t.id, COUNT(hijo) + CASE WHEN t.estado = 0 THEN 1 ELSE 0 END FROM Ticket t LEFT JOIN t.asientos hijo ON hijo.estado = 0 WHERE t.id IN :idsPalcos AND (t.estado = 0 OR hijo.id IS NOT NULL) GROUP BY t.id")
    List<Object[]> contarAsientosDisponiblesPorPalcos(@Param("idsPalcos") List<Long> idsPalcos);

    @Query("SELECT t.id, t.localidad.id FROM Ticket t WHERE t.id IN :ticketIds")
    List<Object[]> findTicketIdAndLocalidadIdByTicketIds(@Param("ticketIds") List<Long> ticketIds);

    //-------------FIN MÉTODOS PARA MAPAS------------------------------------------------------------------

    public Integer countByTarifaIdAndEstado(Long tarifaId, Integer estado);

    /**
     * Encuentra todos los tickets de un cliente que estén en estado 1
     * y cuyos eventos no estén en el estado especificado
     * @param numeroDocumento Número de documento del cliente
     * @param estadoEvento Estado del evento a excluir
     * @return Lista de tickets del cliente con estado 1 y eventos no en el estado dado
     */
    @Query("SELECT DISTINCT t FROM Ticket t " +
            "JOIN FETCH t.localidad l " +
            "JOIN FETCH l.dias d " +
            "JOIN FETCH d.evento e " +
            "WHERE t.cliente.numeroDocumento = :numeroDocumento " +
            "AND t.estado = 1 " +
            "AND e.estado != :estadoEvento " +
            "ORDER BY e.id, t.id")
    List<Ticket> findByClienteNumeroDocumentoAndEventoEstadoNot(
            @Param("numeroDocumento") String numeroDocumento,
            @Param("estadoEvento") Integer estadoEvento
    );
}
