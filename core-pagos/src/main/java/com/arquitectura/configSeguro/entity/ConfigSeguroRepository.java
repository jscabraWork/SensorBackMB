package com.arquitectura.configSeguro.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigSeguroRepository extends JpaRepository<ConfigSeguro,Long> {

    List<ConfigSeguro> findByProveedor(String proveedor);

    List<ConfigSeguro> findByEstado(Integer estado);

    ConfigSeguro findFirstByEstado(Integer estado);

}
