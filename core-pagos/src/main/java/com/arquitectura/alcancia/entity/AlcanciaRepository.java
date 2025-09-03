package com.arquitectura.alcancia.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlcanciaRepository extends JpaRepository<Alcancia,Long> {

    List<Alcancia> findByClienteNumeroDocumentoAndEstado(String pClienteId, Integer estado);


    List<Alcancia> findByClienteNumeroDocumento(String pClienteId);

}
