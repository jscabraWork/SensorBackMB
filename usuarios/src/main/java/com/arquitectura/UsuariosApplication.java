package com.arquitectura;


import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@EnableFeignClients
@SpringBootApplication
@EntityScan({
		"com.arquitectura.codigo_validacion.entity",
		"com.arquitectura.rol.entity",
		"com.arquitectura.usuario.entity",
		"com.arquitectura.recuperacion.entity",
		"com.arquitectura.tipo_documento.entity",
		"com.arquitectura.intento_registro.entity"
	})
public class UsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosApplication.class, args);
	}
	   @PostConstruct
	    void started() {
	      TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	    }

}
