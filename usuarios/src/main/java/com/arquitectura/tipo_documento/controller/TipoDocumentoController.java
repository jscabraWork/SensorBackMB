package com.arquitectura.tipo_documento.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.tipo_documento.entity.TipoDocumento;
import com.arquitectura.tipo_documento.service.TipoDocumentoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tipo-documento")
public class TipoDocumentoController extends CommonController<TipoDocumento, TipoDocumentoService> {
}
