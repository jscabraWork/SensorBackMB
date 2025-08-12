package com.arquitectura.transaccion.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    public Transaccion findByStatusAndOrdenId(int status, Long id);
}
