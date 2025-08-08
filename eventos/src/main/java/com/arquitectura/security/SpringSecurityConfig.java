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
                        .requestMatchers(HttpMethod.GET, "/eventos/**").permitAll()

                        // SOLO-ADMIN
                        .requestMatchers(HttpMethod.POST, "/eventos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/eventos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/eventos/**").hasRole("ADMIN")

                        // ADMIN Y ORGANIZADORES


                        //-------------- TEMPORADAS ------------------
                        .requestMatchers(HttpMethod.GET, "/temporadas/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/temporadas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/temporadas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/temporadas/**").hasRole("ADMIN")

                        //----------------DIAS------------------------
                        .requestMatchers(HttpMethod.GET, "/dia/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/dia/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/dia/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/dia/**").hasRole("ADMIN")

                        //------------------LOCALIDADES----------------
                        .requestMatchers(HttpMethod.GET, "/localidades/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/localidades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/localidades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/localidades/**").hasRole("ADMIN")

                        //-----------------------TARIFAS---------------------
                        .requestMatchers(HttpMethod.GET, "/tarifas/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/tarifas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tarifas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tarifas/**").hasRole("ADMIN")


                        //------------------------CIUDAD----------------------
                        .requestMatchers(HttpMethod.GET, "/ciudades/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/ciudades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/ciudades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ciudades/**").hasRole("ADMIN")

                        //-----------------------VENUE--------------------------
                        .requestMatchers(HttpMethod.GET, "/venues/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/venues/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/venues/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/venues/**").hasRole("ADMIN")

                        //------------------------TIPO (DE EVENTO)---------------
                        .requestMatchers(HttpMethod.GET, "/tipos/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/tipos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tipos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tipos/**").hasRole("ADMIN")


                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
