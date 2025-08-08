package com.arquitectura.cliente.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {

    public Cliente findByCorreo(String pCorreo);

    public Cliente findByNumeroDocumento(String pNumeroDocumento);

}
