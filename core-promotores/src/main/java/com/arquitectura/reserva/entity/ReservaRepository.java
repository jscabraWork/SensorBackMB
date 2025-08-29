package com.arquitectura.reserva.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    //Lista las reservas por evento y promotor
    List<Reserva> findByLocalidadDiasEventoIdAndPromotorNumeroDocumentoAndActiva(Long eventoId, String promotorNumeroDocumento, boolean activa);

    List<Reserva> findByLocalidadDiasEventoIdAndPromotorNumeroDocumento(Long eventoId, String promotorNumeroDocumento);

}
