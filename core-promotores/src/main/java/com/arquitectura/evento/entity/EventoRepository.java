package com.arquitectura.evento.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Encuentra todos los eventos asociados a un promotor específico
     * @param promotorId ID del promotor (número de documento)
     * @return Lista de eventos asociados al promotor
     */
    @Query("SELECT e FROM Evento e JOIN e.promotores p WHERE p.numeroDocumento = :promotorId")
    List<Evento> findByPromotorId(@Param("promotorId") String promotorId);

    List<Evento> findByEstadoNot(int estado);

    List<Evento> findByEstado(int estado);

    /**
     * Encuentra todos los eventos asociados a un promotor específico y estado
     * @param numeroDocumento ID del promotor (número de documento)
     * @param estado Estado del evento
     * @return Lista de eventos asociados al promotor y estado
     */
    List<Evento> findByPromotoresNumeroDocumentoAndEstado(String numeroDocumento, Integer estado);


    /**
     * Encuentra todos los eventos asociados a un promotor específico y estado
     * @param numeroDocumento ID del promotor (número de documento)
     * @param estado Estado del evento
     * @return Lista de eventos asociados al promotor y estado
     */
    List<Evento> findByPromotoresNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer estado);
}
