package com.arquitectura.seguro.service;

import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.services.CommonService;

public interface SeguroService extends CommonService<Seguro> {

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda un seguro y publica el evento en Kafka
     * @param pSeguro El seguro a guardar
     * @return El seguro guardado
     */
    public Seguro saveKafka(Seguro pSeguro);

    /**
     * Elimina un seguro por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del seguro a eliminar
     */
    public void deleteById(Long pId);

}
