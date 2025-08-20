package com.arquitectura.promotor.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotorRepository extends JpaRepository<Promotor, String> {

    /**
     * Encuentra todos los promotores asociados a un evento específico
     * @param eventoId ID del evento
     * @return Lista de promotores asociados al evento
     */
    @Query("SELECT p FROM Promotor p JOIN p.eventos e WHERE e.id = :eventoId")
    List<Promotor> findByEventoId(@Param("eventoId") Long eventoId);

    /**
     * Filtra promotores por nombre, número de documento o correo usando LIKE
     * @param nombre Texto a buscar en el nombre del promotor
     * @param numeroDocumento Texto a buscar en el número de documento
     * @param correo Texto a buscar en el correo del promotor
     * @return Lista de promotores que coinciden con los filtros
     */
    @Query("SELECT p FROM Promotor p WHERE " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:numeroDocumento IS NULL OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :numeroDocumento, '%'))) AND " +
           "(:correo IS NULL OR LOWER(p.correo) LIKE LOWER(CONCAT('%', :correo, '%')))")
    List<Promotor> findByFiltro(@Param("nombre") String nombre,
                                @Param("numeroDocumento") String numeroDocumento,
                                @Param("correo") String correo);

    /**
     * Busca un promotor primero por correo, si no lo encuentra busca por número de documento
     * @param identificador Puede ser correo electrónico o número de documento del promotor
     * @return Optional con el promotor si lo encuentra
     */
    @Query("SELECT p FROM Promotor p WHERE p.correo = :identificador OR p.numeroDocumento = :identificador")
    Optional<Promotor> findByCorreoOrNumeroDocumento(@Param("identificador") String identificador);

}
