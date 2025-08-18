package com.arquitectura.promotor.service;

import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.services.CommonServiceString;

import java.util.List;

public interface PromotorService extends CommonServiceString<Promotor> {

    public List<Promotor> findByEvento(Long pEventoid);

    public Promotor asignarEventos(String numeroDocumento, List<Long> pEventosId);

    public List<Promotor> findByFiltro(String nombre, String numeroDocumento, String correo);
}


