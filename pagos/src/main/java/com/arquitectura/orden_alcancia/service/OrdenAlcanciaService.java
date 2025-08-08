package com.arquitectura.orden_alcancia.service;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.services.CommonService;

public interface OrdenAlcanciaService extends CommonService<OrdenAlcancia> {

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda una orden alcancía y publica el evento en Kafka
     * @param pOrdenAlcancia La orden alcancía a guardar
     * @return La orden alcancía guardada
     */
    public OrdenAlcancia saveKafka(OrdenAlcancia pOrdenAlcancia);

    /**
     * Elimina una orden alcancía por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la orden alcancía a eliminar
     */
    public void deleteById(Long pId);

    public void confirmarCreacion(Orden orden, Double pAporte) throws Exception;

    public void confirmarAporte(Orden orden, Double pAporte) throws Exception;

}
