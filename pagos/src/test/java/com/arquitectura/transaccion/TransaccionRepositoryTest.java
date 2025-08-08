package com.arquitectura.transaccion;

import com.arquitectura.PagosApplication;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TransaccionRepositoryTest {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private TestEntityManager entityManager;


    @BeforeEach
    void setup() {

    }

    @Test
    @DisplayName("Buscar transacción por status y orden ID - Éxito")
    void findByStatusAndOrdenId_deberiaRetornarTransaccionCuandoExiste() {
        Orden orden = new Orden();
        entityManager.persist(orden);

        Transaccion transaccion = new Transaccion();
        transaccion.setStatus(1);
        transaccion.setOrden(orden);
        entityManager.persist(transaccion);
        entityManager.flush();

        Transaccion encontrada = transaccionRepository.findByStatusAndOrdenId(1, orden.getId());

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getStatus()).isEqualTo(1);
        assertThat(encontrada.getOrden().getId()).isEqualTo(orden.getId());
    }

    @Test
    @DisplayName("Buscar transacción por status y orden ID - No encontrada")
    void findByStatusAndOrdenId_deberiaRetornarNullCuandoNoExiste() {

        Transaccion encontrada = transaccionRepository.findByStatusAndOrdenId(1, 999L);

        assertThat(encontrada).isNull();
    }


}
