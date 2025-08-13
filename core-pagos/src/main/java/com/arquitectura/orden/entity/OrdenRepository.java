package com.arquitectura.orden.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /**
     * Trae todos las ordenes por el cliente id
     * @param numeroDocumento El ID del cliente
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public List<Orden> findByClienteNumeroDocumento(String numeroDocumento);

    public List<Orden> findByEstado(Integer estado);

    @Query("SELECT o FROM Orden o " +
            "WHERE o.idTRXPasarela IS NOT NULL " +
            "AND NOT EXISTS ( " +
            "    SELECT t FROM Transaccion t " +
            "    WHERE t.orden.id = o.id " +
            "    AND t.status IN (34, 36, 50, 8, 5, 4)" +
            ")")
    public	List<Orden> findAllOrdenesSinConfirmacion();
}
