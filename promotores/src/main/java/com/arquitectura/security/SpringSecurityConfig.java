package com.arquitectura.security;

import com.arquitectura.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(exchange -> exchange

                        //----------------EVENTOS---------------------
                        //TODOS
                        .requestMatchers(HttpMethod.GET, "/reservas/*").permitAll()

                        //GET ERS PARA REPORTE
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "PROMOTOR", "ORGANIZADOR", "CONTADOR")

                        // SOLO-ADMIN
                        .requestMatchers(HttpMethod.POST, "/reservas/**").hasRole("PROMOTOR")
                        .requestMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").denyAll()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
