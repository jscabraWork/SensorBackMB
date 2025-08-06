package com.arquitectura.tipo_documento.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.tipo_documento.entity.TipoDocumento;
import com.arquitectura.tipo_documento.entity.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

@Service
public class TipoDocumentoServiceImpl extends CommonServiceImpl<TipoDocumento, TipoDocumentoRepository> implements TipoDocumentoService {

    public TipoDocumento findByNombre(String pNombre) {
        return repository.findByNombre(pNombre);
    }
}
