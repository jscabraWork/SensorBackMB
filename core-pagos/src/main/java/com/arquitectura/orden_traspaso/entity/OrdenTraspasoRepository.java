package com.arquitectura.orden_traspaso.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenTraspasoRepository extends JpaRepository<OrdenTraspaso, Long> {
}
