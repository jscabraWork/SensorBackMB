package com.arquitectura.ingreso.service;

import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.services.CommonService;

public interface IngresoService extends CommonService<Ingreso> {

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda un ingreso y publica el evento en Kafka
     * @param pIngreso El ingreso a guardar
     * @return El ingreso guardado
     */
    public Ingreso saveKafka(Ingreso pIngreso);

    /**
     * Elimina un ingreso por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del ingreso a eliminar
     */
    public void deleteById(Long pId);

}
