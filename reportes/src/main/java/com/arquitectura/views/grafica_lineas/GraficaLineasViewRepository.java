package com.arquitectura.views.grafica_lineas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraficaLineasViewRepository extends JpaRepository<GraficaLineasView, Long> {


}
