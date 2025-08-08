package com.arquitectura.orden_alcancia.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenAlcanciaRepository extends JpaRepository<OrdenAlcancia,Long> {

    //Sirve para encontrar la orden que creo la alcancia
    OrdenAlcancia findByTipoAndAlcanciaIdAndEstado(Integer tipo, Long alcanciaId, Integer estado);
}
