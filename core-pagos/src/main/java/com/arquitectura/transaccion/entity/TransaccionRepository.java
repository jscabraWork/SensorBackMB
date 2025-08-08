package com.arquitectura.transaccion.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    public Transaccion findByStatusAndOrdenId(int status, Long id);

    public List<Transaccion> findByOrdenId(Long id);

}
