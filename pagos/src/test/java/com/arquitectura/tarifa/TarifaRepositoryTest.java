package com.arquitectura.tarifa;

import com.arquitectura.PagosApplication;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TarifaRepositoryTest {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Tarifa tarifa;

    @BeforeEach
    void setup() {
        tarifa = Tarifa.builder()
                .nombre("Tarifa de prueba")
                .precio(100.0)
                .servicio(10.0)
                .iva(19.0)
                .estado(1)
                .build();

        testEntityManager.persist(tarifa);
        testEntityManager.flush();
    }

    // Test CRUD básicos
    @Test
    @DisplayName("Buscar tarifa por ID existente")
    void testFindById() {
        Tarifa encontrada = tarifaRepository.findById(tarifa.getId()).orElse(null);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getNombre()).isEqualTo("Tarifa de prueba");
        assertThat(encontrada.getPrecio()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Buscar tarifa por ID no existente")
    void testFindByIdNotFound() {
        Tarifa noEncontrada = tarifaRepository.findById(999L).orElse(null);

        assertThat(noEncontrada).isNull();
    }

    @Test
    @DisplayName("Guardar una nueva tarifa")
    void testSave() {
        Tarifa nuevaTarifa = Tarifa.builder()
                .nombre("Nueva Tarifa")
                .precio(200.0)
                .servicio(20.0)
                .iva(19.0)
                .estado(1)
                .build();

        Tarifa guardada = tarifaRepository.save(nuevaTarifa);

        assertThat(guardada.getId()).isNotNull();
        assertThat(tarifaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Actualizar una tarifa existente")
    void testUpdate() {
        Tarifa encontrada = tarifaRepository.findById(tarifa.getId()).orElse(null);
        assertThat(encontrada).isNotNull();

        encontrada.setNombre("Tarifa actualizada");
        encontrada.setPrecio(150.0);
        tarifaRepository.save(encontrada);

        Tarifa actualizada = tarifaRepository.findById(tarifa.getId()).orElse(null);
        assertThat(actualizada.getNombre()).isEqualTo("Tarifa actualizada");
        assertThat(actualizada.getPrecio()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Eliminar una tarifa existente")
    void testDelete() {
        Long id = tarifa.getId();
        tarifaRepository.deleteById(id);

        Tarifa eliminada = tarifaRepository.findById(id).orElse(null);
        assertThat(eliminada).isNull();
    }

    @Test
    @DisplayName("Verificar si tarifa tiene tickets asociados - Sin tickets")
    void testExistsTicketsByTarifaId_WhenNoTickets() {
        boolean resultado = tarifaRepository.existsTicketsByTarifaId(tarifa.getId());

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Verificar si tarifa tiene tickets asociados - Con tickets")
    void testExistsTicketsByTarifaId_WhenHasTickets() {
        // Crear ticket asociado
        Ticket ticket = Ticket.builder()
                .tarifa(tarifa)
                // otros campos requeridos del ticket
                .build();

        testEntityManager.persist(ticket);
        testEntityManager.flush();

        boolean resultado = tarifaRepository.existsTicketsByTarifaId(tarifa.getId());

        assertThat(resultado).isTrue();
    }

}
