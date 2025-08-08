package com.arquitectura.venue;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.ciudad.entity.Ciudad;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class VenueRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Venue venue;

    private Ciudad ciudad;

    @BeforeEach
    void setup() {

        ciudad = Ciudad.builder().nombre("Ciudad Test").build();

        testEntityManager.persist(ciudad);

        venue = Venue.builder()
                        .nombre("Bar Negro")
                        .urlMapa("http://googlemaps.com")
                        .ciudad(ciudad)
                        .eventos(null)
                        .build();

        testEntityManager.persist(venue);
        testEntityManager.flush();
    }

    @Test
    void buscarPorNombre(){
        Venue venueEncontrada = venueRepository.findByNombre("Bar Negro").orElse(null);
        assertThat(venueEncontrada).isNotNull();
        assertThat(venueEncontrada.getNombre()).isEqualTo("Bar Negro");
    }

    @Test
    void buscarPorNombreNoEncontrado(){
        Venue venueNoEncontrada = venueRepository.findByNombre("Bar Blanco").orElse(null);
        assertThat(venueNoEncontrada).isNull();
    }

    @Test
    @DisplayName("Buscar venue por ID existente")
    void testFindById() {
        Long venueId = venue.getId();
        Venue encontrada = venueRepository.findById(venueId).orElse(null);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getNombre()).isEqualTo("Bar Negro");
    }

    @Test
    @DisplayName("Buscar venue por ID no existente")
    void testFindByIdNotFound() {
        Venue noEncontrada = venueRepository.findById(999L).orElse(null);
        assertThat(noEncontrada).isNull();
    }

    @Test
    @DisplayName("Buscar venue por nombre y ciudad ID - encontrado")
    void testFindByNombreAndCiudadIdEncontrado() {
        Optional<Venue> venue = venueRepository.findByNombreAndCiudadId("Bar Negro", ciudad.getId());
        assertThat(venue).isPresent();
        assertThat(venue.get().getNombre()).isEqualTo("Bar Negro");
        assertThat(venue.get().getCiudad().getId()).isEqualTo(ciudad.getId());
    }

    @Test
    @DisplayName("Buscar venue por nombre y ciudad ID - no encontrado")
    void testFindByNombreAndCiudadIdNoEncontrado() {
        Optional<Venue> venue = venueRepository.findByNombreAndCiudadId("Nombre Inexistente", ciudad.getId());
        assertThat(venue).isEmpty();

        Optional<Venue> venue2 = venueRepository.findByNombreAndCiudadId("Bar Negro", 999L);
        assertThat(venue2).isEmpty();
    }

    @Test
    @DisplayName("Buscar venues por ciudad ID - encontrados")
    void testFindByCiudadIdEncontrados() {
        Venue otroVenue = Venue.builder()
                .nombre("Otro Bar")
                .urlMapa("http://otromapa.com")
                .ciudad(ciudad)
                .build();
        testEntityManager.persist(otroVenue);
        testEntityManager.flush();

        List<Venue> venues = venueRepository.findByCiudadId(ciudad.getId());
        assertThat(venues).isNotEmpty();
        assertThat(venues).hasSize(2);
        assertThat(venues)
                .extracting(Venue::getCiudad)
                .extracting(Ciudad::getId)
                .containsOnly(ciudad.getId());
    }

    @Test
    @DisplayName("Buscar venues por ciudad ID - no encontrados")
    void testFindByCiudadIdNoEncontrados() {
        Ciudad otraCiudad = Ciudad.builder().nombre("Otra Ciudad").build();
        testEntityManager.persist(otraCiudad);
        testEntityManager.flush();

        List<Venue> venues = venueRepository.findByCiudadId(otraCiudad.getId());
        assertThat(venues).isEmpty();
    }

    @Test
    @DisplayName("Buscar venues por ciudadId existente")
    void testFindByCiudadId() {
        Ciudad ciudadTest = Ciudad.builder().nombre("Guarapo").build();
        testEntityManager.persist(ciudadTest);

        Venue venue1 = Venue.builder()
                .nombre("Venue 1")
                .urlMapa("http://venue1")
                .ciudad(ciudadTest)
                .eventos(null)
                .build();

        Venue venue2 = Venue.builder()
                .nombre("Venue 2")
                .urlMapa("http://venue2")
                .ciudad(ciudadTest)
                .eventos(null)
                .build();

        testEntityManager.persist(venue1);
        testEntityManager.persist(venue2);
        testEntityManager.flush();

        List<Venue> venues = venueRepository.findByCiudadId(ciudadTest.getId());

        assertThat(venues).isNotEmpty();
        assertThat(venues).hasSize(2);
        assertThat(venues.get(0).getNombre()).isEqualTo("Venue 1");
        assertThat(venues.get(1).getNombre()).isEqualTo("Venue 2");
    }

    @Test
    @DisplayName("Buscar venues por ciudadId inexistente")
    void testFindByCiudadIdNotFound() {
        List<Venue> venues = venueRepository.findByCiudadId(999L);

        assertThat(venues).isEmpty();
    }

}
