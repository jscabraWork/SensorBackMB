package com.arquitectura;

import com.arquitectura.imagen.service.ImagenService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;
@EntityScan({
		"com.arquitectura.*",
})
@EnableJpaRepositories
@EnableTransactionManagement
@SpringBootApplication
@EnableFeignClients
public class PagosApplication implements CommandLineRunner {

	@Resource
	ImagenService fileService;

	public static void main(String[] args) {
		SpringApplication.run(PagosApplication.class, args);
	}

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	@Override
	public void run(String... args) throws Exception {
		fileService.deleteAll();
		fileService.init();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
