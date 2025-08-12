package com.arquitectura.tipo_documento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    public TipoDocumento findByNombre(String pNombre);
}
