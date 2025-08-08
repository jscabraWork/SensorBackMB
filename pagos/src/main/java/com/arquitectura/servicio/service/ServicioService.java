package com.arquitectura.servicio.service;

import com.arquitectura.services.CommonService;
import com.arquitectura.servicio.entity.Servicio;

public interface ServicioService extends CommonService<Servicio> {

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda un servicio y publica el evento en Kafka
     * @param pServicio El servicio a guardar
     * @return El servicio guardado
     */
    public Servicio saveKafka(Servicio pServicio);

    /**
     * Elimina un servicio por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del servicio a eliminar
     */
    public void deleteById(Long pId);

}
