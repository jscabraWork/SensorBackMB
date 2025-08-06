package com.arquitectura;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.TimeZone;

@SpringBootApplication
@EntityScan({
		"com.arquitectura.*"
})
public class PuntosFisicosConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuntosFisicosConsumerApplication.class, args);
	}
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

}
