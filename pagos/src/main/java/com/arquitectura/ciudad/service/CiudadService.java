package com.arquitectura.ciudad.service;

import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.services.CommonService;

public interface CiudadService extends CommonService<Ciudad> {

    /*** Crea una ciudad*/
    public Ciudad crear (Ciudad ciudad);

    /*** Actualiza una ciudad*/
    public Ciudad actualizar (Long pId,Ciudad ciudad);

    /**
     * Elimina una ciudad por su ID
     * @param pId El ID de la ciudad a eliminar
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    public void deleteById(Long pId);

    public Ciudad saveKafka(Ciudad pCiudad);

}

