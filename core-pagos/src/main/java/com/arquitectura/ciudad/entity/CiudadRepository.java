package com.arquitectura.ciudad.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad,Long> {

    Optional<Ciudad> findByNombre(String nombre);


}
