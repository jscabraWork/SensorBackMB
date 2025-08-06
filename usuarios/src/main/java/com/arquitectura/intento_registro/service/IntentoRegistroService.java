package com.arquitectura.intento_registro.service;

import com.arquitectura.intento_registro.entity.IntentoRegistro;

public interface IntentoRegistroService {

    public IntentoRegistro crearIntentoRegistro(IntentoRegistro pIntentoRegistro) throws Exception;

    public IntentoRegistro getIntentoRegistro(String pIdIntentoRegistro);

    public IntentoRegistro save(IntentoRegistro pIntentoRegistro);

}
