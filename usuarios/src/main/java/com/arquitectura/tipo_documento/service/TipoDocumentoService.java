package com.arquitectura.tipo_documento.service;

import com.arquitectura.services.CommonService;
import com.arquitectura.tipo_documento.entity.TipoDocumento;

public interface TipoDocumentoService extends CommonService<TipoDocumento> {

    public TipoDocumento findByNombre(String pNombre);
}
