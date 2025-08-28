package com.arquitectura.reserva.service;

import com.arquitectura.reserva.entity.Reserva;
import com.arquitectura.services.CommonService;

import java.util.List;

public interface ReservaService extends CommonService<Reserva> {

    public Reserva crear(Reserva reserva, Long pLocalidadId, String pPromotorId);

    public List<Reserva> findByPromotorEventoAndEstado(Long eventoId, String promotorNumeroDocumento, boolean activa);

    public List<Reserva> findByPromotorAndEvento(Long eventoId, String promotorNumeroDocumento);

}
