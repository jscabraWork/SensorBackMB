package com.arquitectura.reserva.service;

import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.service.PromotorService;
import com.arquitectura.reserva.entity.Reserva;
import com.arquitectura.reserva.entity.ReservaRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaServiceImpl extends CommonServiceImpl<Reserva, ReservaRepository> implements ReservaService {

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private PromotorService promotorService;

    @Override
    public Reserva crear(Reserva reserva, Long pLocalidadId, String pPromotorId) {

        Promotor promotor = promotorService.findById(pPromotorId);
        if (promotor == null) {
            throw new IllegalArgumentException("Promotor no encontrado con id: " + pPromotorId);
        }

        Localidad localidad = localidadService.findById(pLocalidadId);
        if (localidad == null) {
            throw new IllegalArgumentException("Localidad no encontrada con id: " + pLocalidadId);
        }

        Reserva reservaNew = new Reserva(
            reserva.getClienteId(),
            true,
            reserva.getCantidad(),
            localidad,
            promotor
        );

        return save(reservaNew);
    }

    public List<Reserva> findByPromotorEventoAndEstado(Long eventoId, String promotorNumeroDocumento, boolean activa){
        return repository.findByLocalidadDiasEventoIdAndPromotorNumeroDocumentoAndActiva(eventoId, promotorNumeroDocumento, activa);
    }


}
