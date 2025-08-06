package com.arquitectura.puntofisico.services;

import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.services.CommonServiceString;

import java.util.List;

public interface PuntoFisicoService extends CommonServiceString<PuntoFisico> {

    public List<PuntoFisico> findByEvento(Long pEventoid);

    public PuntoFisico asignarEventos(String numeroDocumento, List<Long> pEventosId);

    public List<PuntoFisico> findByFiltro(String nombre, String numeroDocumento, String correo);

}
