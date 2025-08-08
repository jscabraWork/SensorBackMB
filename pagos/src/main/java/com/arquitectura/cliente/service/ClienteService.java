package com.arquitectura.cliente.service;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.services.CommonServiceString;

import java.util.List;

public interface ClienteService extends CommonServiceString<Cliente> {

    public Cliente findByCorreo(String pCorreo);

    public Cliente findByNumeroDocumento(String pNumeroDocumento);

    public String obtenerUsuarioDeToken(String pBearerToken);

    public String obtenerRolDeToken(String pBearerToken);

    public List<Cliente> findAllClientesById(List<String> pClientesIds);


}
