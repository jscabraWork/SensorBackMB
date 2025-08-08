package com.arquitectura.transaccion;

import com.arquitectura.PagosApplication;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import com.arquitectura.transaccion.service.TransaccionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TransaccionServiceTest {

    @Autowired
    private TransaccionService transaccionService;

    @MockBean
    private TransaccionRepository transaccionRepository;


    @Test
    @DisplayName("Obtener transacción repetida - Transacción existe")
    void obtenerTransaccionRepetida() {
        Orden orden = new Orden();
        orden.setId(1L);

        Transaccion transaccion = new Transaccion();
        transaccion.setStatus(1);
        transaccion.setOrden(orden);

        when(transaccionRepository.findByStatusAndOrdenId(1, 1L))
                .thenReturn(transaccion);

        Transaccion resultado = transaccionService.getTransaccionRepetida(1, 1L);

        assertThat(resultado)
                .as("La transacción no debería ser null")
                .isNotNull();

        assertThat(resultado.getOrden())
                .as("La orden no debería ser null")
                .isNotNull();

        assertThat(resultado.getStatus())
                .as("El status debería ser 1")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Obtener transacción repetida - Transacción no existe")
    void getTransaccionRepetida_DeberiaRetornarNullCuandoNoExiste() {
        when(transaccionRepository.findByStatusAndOrdenId(1, 999L))
                .thenReturn(null);
        Transaccion resultado = transaccionService.getTransaccionRepetida(1, 999L);
        assertThat(resultado)
                .as("Debería retornar null cuando no existe")
                .isNull();
    }

}
