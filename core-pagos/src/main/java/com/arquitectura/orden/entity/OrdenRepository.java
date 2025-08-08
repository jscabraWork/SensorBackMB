package com.arquitectura.orden.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /**
     * Trae todos las ordenes por el cliente id
     * @param numeroDocumento El ID del cliente
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public List<Orden> findByClienteNumeroDocumento(String numeroDocumento);
}
