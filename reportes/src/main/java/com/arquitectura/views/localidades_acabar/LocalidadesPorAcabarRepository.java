package com.arquitectura.views.localidades_acabar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalidadesPorAcabarRepository extends JpaRepository<LocalidadesPorAcabar, Long> {
}
