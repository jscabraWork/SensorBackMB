package com.arquitectura.cupon.service;

import com.arquitectura.cupon.entity.Cupon;
import com.arquitectura.services.CommonServiceString;
import com.arquitectura.tarifa.entity.Tarifa;

import java.util.List;

public interface CuponService extends CommonServiceString<Cupon> {

    public List<Cupon> findByTarifaId(Long tarifaId) throws Exception;

    public Cupon actualizar(String pCuponId, Cupon pCupon) throws Exception;

    public Cupon crear(Cupon cupon);

    public Cupon validarCupon(String codigo, Tarifa tarifa, Integer cantidadTickets);
}
