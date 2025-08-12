package com.arquitectura.orden_promotor.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenPromotorRepository extends JpaRepository<OrdenPromotor, Long> {

    /**
     * Encuentra todas las órdenes de promotor por número de documento del cliente
     */
    List<OrdenPromotor> findByClienteNumeroDocumento(String numeroDocumento);

    /**
     * Encuentra todas las órdenes de promotor por número de documento del promotor
     */
    List<OrdenPromotor> findByPromotorNumeroDocumento(String promotorNumeroDocumento);
}
