package com.arquitectura.ciudad;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class CiudadRepositoryTest {

    @Autowired
    private CiudadRepository ciudadRepository;

    @MockBean
    private VenueRepository venueRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Ciudad ciudad;

    @BeforeEach
    void setup() {
        ciudad = Ciudad.builder()
                        .nombre("Neiva")
                        .venues(null)
                        .build();

        testEntityManager.persist(ciudad);
        testEntityManager.flush();
    }

    @Test
    void buscarPorNombre(){
        Ciudad ciudadEncontrada = ciudadRepository.findByNombre("Neiva").orElse(null);
        assertThat(ciudadEncontrada).isNotNull();
        assertThat(ciudadEncontrada.getNombre()).isEqualTo("Neiva");
    }

    @Test
    void buscarPorNombreNoEncontrado(){
        Ciudad ciudadNoEncontrada = ciudadRepository.findByNombre("Ciudad Baja").orElse(null);
        assertThat(ciudadNoEncontrada).isNull();
    }

    @Test
    @DisplayName("Buscar ciudad por ID existente")
    void testFindById() {
        Ciudad encontrada = ciudadRepository.findById(ciudad.getId()).orElse(null);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getNombre()).isEqualTo("Neiva");
    }

    @Test
    @DisplayName("Buscar ciudad por ID no existente")
    void testFindByIdNotFound() {
        Ciudad noEncontrada = ciudadRepository.findById(999L).orElse(null);
        assertThat(noEncontrada).isNull();
    }

    @Test
    @DisplayName("Buscar todas las ciudades")
    void testFindAll() {
        List<Ciudad> ciudades = ciudadRepository.findAll();

        assertThat(ciudades).isNotEmpty();
        assertThat(ciudades);
    }

    @Test
    @DisplayName("Eliminar una ciudad exitosamente")
    void deleteByIdFound(){
        Ciudad ciudadSinVenues = ciudad;
        ciudadRepository.deleteById(ciudadSinVenues.getId());
        assertFalse(ciudadRepository.existsById(ciudadSinVenues.getId()));
    }

    @Test
    @DisplayName("trata de eliminar una ciudad que no existe")
    void deleteByIdNotFound() {
        Long nonExistentId = 999L;
        assertDoesNotThrow(() -> {
            ciudadRepository.deleteById(nonExistentId);
        });
        assertTrue(ciudadRepository.existsById(ciudad.getId()));
    }


}
