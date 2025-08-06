package com.arquitectura.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(name="microservicio-auditores")
public interface AuditoresFeignClient {

    //Crear auditor en microservicio-auditores
    @PostMapping("/auditores/crear")
    public ResponseEntity<?> crearAuditor(
            @RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("nombre") String nombre);

    //Actualizar auditor en microservicio-auditores
    @PutMapping("/auditores")
    public ResponseEntity<?> actualizarAuditor(
            @RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("nombre") String nombre);

    }


