package com.arquitectura.orden_puntofisico.entity;

import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenPuntoFisicoRepository extends JpaRepository<OrdenPuntoFisico, Long> {

    /**
     * Encuentra todas las órdenes de punto físico por número de documento del cliente
     */
    List<OrdenPuntoFisico> findByClienteNumeroDocumento(String numeroDocumento);

    /**
     * Encuentra todas las órdenes de punto físico por número de documento del punto físico
     */
    List<OrdenPuntoFisico> findByPuntoFisicoNumeroDocumento(String puntoFisicoNumeroDocumento);
}
