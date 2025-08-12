package com.arquitectura.venue.entity;

import com.arquitectura.ciudad.entity.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue,Long> {

    Optional<Venue> findByNombre(String nombre);

    List<Venue> findByCiudadId(Long ciudadId);

    Optional<Venue> findByNombreAndCiudadId(String nombre, Long ciudadId);
}
