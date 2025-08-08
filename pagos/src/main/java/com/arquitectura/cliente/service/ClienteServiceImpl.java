package com.arquitectura.cliente.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.services.CommonServiceImplString;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;

@Service
public class ClienteServiceImpl extends CommonServiceImplString<Cliente, ClienteRepository> implements ClienteService {

    @Override
    public Cliente findByCorreo(String pCorreo) {
        return repository.findByCorreo(pCorreo);
    }

    public Cliente findByNumeroDocumento(String pNumeroDocumento) {
        Cliente cliente = repository.findByNumeroDocumento(pNumeroDocumento);
        if(cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con el numero de documento: " + pNumeroDocumento);
        }
        return cliente;
    }

    @Override
    public String obtenerUsuarioDeToken(String pBearerToken) {
        try {
            String[] tokenParts = pBearerToken.split("\\.");
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
            JSONObject payloadJson = new JSONObject(payload);
            String usuarioNumeroDocumento= payloadJson.getString("cc");
            return usuarioNumeroDocumento;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no autorizado");
        }
    }

    @Override
    public String obtenerRolDeToken(String pBearerToken) {
        try {
            String[] tokenParts = pBearerToken.split("\\.");

            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));

            JSONObject payloadJson = new JSONObject(payload);

            List<Object> authorities = payloadJson.getJSONArray("authorities").toList();

            if (!authorities.isEmpty()) {
                return authorities.get(0).toString();
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public List<Cliente> findAllClientesById(List<String> pClientesIds) {
        return repository.findAllById(pClientesIds);
    }
}


