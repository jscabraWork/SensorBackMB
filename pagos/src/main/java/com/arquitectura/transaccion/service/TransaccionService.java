package com.arquitectura.transaccion.service;

import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.services.CommonService;
import com.arquitectura.transaccion.entity.Transaccion;

import java.util.List;

public interface TransaccionService extends CommonService<Transaccion> {

    public Transaccion getTransaccionRepetida(int status, Long orden);

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda una transacción y publica el evento en Kafka
     * @param pTransaccion La transacción a guardar
     * @return La transacción guardada
     */
    public Transaccion saveKafka(Transaccion pTransaccion);

    /**
     * Elimina una transacción por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la transacción a eliminar
     */
    public void deleteById(Long pId);


    public Transaccion crearTransaccionPuntoFisico(OrdenPuntoFisico orden, Integer metodo, Integer status);

    public void saveAllKafka(List<Transaccion> pTransaccion);


}
