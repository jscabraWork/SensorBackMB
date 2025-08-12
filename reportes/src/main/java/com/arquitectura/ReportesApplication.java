package com.arquitectura;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.TimeZone;

@SpringBootApplication
@EntityScan({
		"com.arquitectura.*"
})
@EnableJpaRepositories
@EnableTransactionManagement
public class ReportesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportesApplication.class, args);
	}
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

}
