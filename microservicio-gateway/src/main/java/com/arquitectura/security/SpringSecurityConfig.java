package com.arquitectura.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.arquitectura.JwtAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;



@Configuration
@EnableWebFluxSecurity
public class SpringSecurityConfig {

	@Autowired
	private JwtAuthenticationFilter autheticationFilter;

	@Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeExchange(exchange -> exchange
                        //PERMISOS EN USUARIOS
						.anyExchange().permitAll()
				)
				.addFilterAt(autheticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.csrf(csrf -> csrf.disable())
				.build();
	}

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:4200");
        corsConfig.addAllowedOrigin("http://localhost:4401");
        corsConfig.addAllowedOrigin("http://localhost:5200");
        corsConfig.addAllowedOrigin("http://10.0.2.2:8090");
        corsConfig.addAllowedOrigin("http://10.0.2.2");
        corsConfig.addAllowedOrigin("https://allticketscol.com");
        corsConfig.addAllowedOrigin("https://www.allticketscol.com");
        corsConfig.addAllowedOrigin("https://allticketses.com");
        corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Corrige "Content-Type"

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
