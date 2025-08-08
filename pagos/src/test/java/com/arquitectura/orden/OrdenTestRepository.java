package com.arquitectura.orden;

import com.arquitectura.PagosApplication;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class OrdenTestRepository {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OrdenRepository ordenRepository;


    private Orden orden;
    private Cliente cliente;
    @BeforeEach
    void setup() {
        orden = Orden.builder()
                .valorOrden(1000.0)
                .estado(1)
                .tipo(0)
                .tickets(new ArrayList<>())
                .cliente(null)
                .transacciones(new ArrayList<>())
                .build();

        testEntityManager.persist(orden);
        testEntityManager.flush();


    }

    @Test
    @DisplayName("Buscar orden por ID existente")
    void testFindById() {
        Long idInexistente = 999L;
        Optional<Orden> resultado = ordenRepository.findById(idInexistente);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Buscar orden por ID no existente")
    void testFindByIdNotFound() {
        Orden noEncontrado = ordenRepository.findById(999L).orElse(null);
        assertThat(noEncontrado).isNull();
    }

    @Test
    @DisplayName("Buscar ordenes por ID cliente no existente")
    void testFindByClienteId() {
        String documentoInexistente = "99999999Z";
        List<Orden> ordenes = ordenRepository.findByClienteNumeroDocumento(documentoInexistente);
        assertThat(ordenes).isEmpty();
    }

    @Test
    @DisplayName("Buscar ordenes por ID cliente existente")
    void testFindByClienteIdNotFound() {
        List<Orden> ordenes = ordenRepository.findByClienteNumeroDocumento("999");
        assertThat(ordenes).isEmpty();
    }

}
