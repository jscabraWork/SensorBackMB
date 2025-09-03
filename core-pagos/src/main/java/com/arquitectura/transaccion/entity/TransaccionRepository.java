package com.arquitectura.transaccion.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    public Transaccion findByStatusAndOrdenId(int status, Long id);

    public List<Transaccion> findByOrdenId(Long id);

    @Query("SELECT t FROM Transaccion t JOIN t.orden o WHERE " +
           "(:numeroDocumento IS NULL OR o.cliente.numeroDocumento LIKE %:numeroDocumento%) AND " +
           "(:correo IS NULL OR t.email LIKE %:correo%) AND " +
           "(:fechaInicio IS NULL OR DATE(t.creationDate) >= DATE(:fechaInicio)) AND " +
           "(:fechaFin IS NULL OR DATE(t.creationDate) <= DATE(:fechaFin)) AND " +
           "(:estado IS NULL OR t.status = :estado) AND " +
           "(:metodo IS NULL OR t.metodo = :metodo) AND " +
           "(:metodoNombre IS NULL OR t.metodoNombre LIKE %:metodoNombre%)")
    Page<Transaccion> findByFiltro(
            @Param("numeroDocumento") String numeroDocumento,
            @Param("correo") String correo,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            @Param("estado") Integer estado,
            @Param("metodo") Integer metodo,
            @Param("metodoNombre") String metodoNombre,
            Pageable pageable
    );

}
