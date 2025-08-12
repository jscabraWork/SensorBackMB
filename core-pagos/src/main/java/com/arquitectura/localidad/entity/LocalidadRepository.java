package com.arquitectura.localidad.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocalidadRepository extends JpaRepository<Localidad,Long> {


    List<Localidad> findAllByNombreIgnoreCase(String nombre);

    List<Localidad> findAllByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Localidad> findByDiasId(Long diaId);

    List<Localidad> findByDiasEventoId(Long pEventoId);

    List<Localidad> findByDiasEventoIdAndDiasEstado(Long pEventoId, int estado);

    @Query("SELECT l FROM Localidad l LEFT JOIN FETCH l.dias WHERE l.id = :id")
    Optional<Localidad> findByIdWithDias(@Param("id") Long id);

    //METODO PARA MAPAS
    //Atentamente: Isaac, lo uso en mapas

    // Trae las localidades distintas asociadas a los tickets
    @Query("SELECT DISTINCT t.localidad FROM Ticket t WHERE t.id IN :ticketIds")
    List<Localidad> findLocalidadesByTicketIds(@Param("ticketIds") List<Long> ticketIds);

}
