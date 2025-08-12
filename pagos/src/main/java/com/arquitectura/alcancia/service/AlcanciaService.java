package com.arquitectura.alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.services.CommonService;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.transaccion.entity.Transaccion;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.List;

public interface AlcanciaService extends CommonService<Alcancia> {

    public Alcancia crear(Cliente cliente, List<Ticket> tickets, Double precioTotal, Tarifa tarifa) throws Exception;

    public Alcancia aportar(Alcancia alcancia, Double aporte) throws Exception;

    //----------------Métodos para Kafka-------------------
    /**
     * Guarda una alcancía y publica el evento en Kafka
     * @param pAlcancia La alcancía a guardar
     * @return La alcancía guardada
     */
    public Alcancia saveKafka(Alcancia pAlcancia);

    /**
     * Elimina una alcancía por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID de la alcancía a eliminar
     */
    public void deleteById(Long pId);

    public List<Alcancia> findActivasByCliente(String pClienteId);

}
