package com.arquitectura.ptp;
import com.arquitectura.transaccion.entity.Transaccion;

public interface PtpAdapter {
    public Transaccion crearTransaccion(RequestResponse request);
}
