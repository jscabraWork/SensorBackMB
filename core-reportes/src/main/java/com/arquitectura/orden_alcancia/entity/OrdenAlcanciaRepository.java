package com.arquitectura.orden_alcancia.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenAlcanciaRepository extends JpaRepository<OrdenAlcancia,Long> {

    //Insertar directamente en la tabla de alcancías usando el ID de la orden existente
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "INSERT INTO ordenes_alcancia (id, alcancia_id) VALUES (:ordenId, :alcanciaId)", nativeQuery = true)
    void insertOrdenAlcancia(@Param("ordenId") Long ordenId, @Param("alcanciaId") Long alcanciaId);

}
