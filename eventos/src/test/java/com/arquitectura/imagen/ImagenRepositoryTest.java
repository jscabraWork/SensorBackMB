package com.arquitectura.imagen;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.tipo.entity.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class ImagenRepositoryTest {

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Imagen imagen;
    private Evento evento;
    private Temporada temporada;
    private Tipo tipo;

    @BeforeEach
    void setup() {
        // Crear entidades relacionadas
        temporada = Temporada.builder().nombre("Temporada Test").build();
        tipo = Tipo.builder().nombre("Concierto").build();

        testEntityManager.persist(temporada);
        testEntityManager.persist(tipo);
        testEntityManager.flush();

        evento = Evento.builder()
                .pulep("EV-2025-001")
                .artistas("Artista Principal")
                .nombre("Evento Test")
                .recomendaciones("Llegar temprano")
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 10, 0))
                .estado(2)
                .temporada(temporada)
                .tipo(tipo)
                .build();

        testEntityManager.persist(evento);
        testEntityManager.flush();

        // NO establecer el ID manualmente - dejar que Hibernate lo genere
        imagen = new Imagen("test-image.jpg", 1, evento);

        testEntityManager.persist(imagen);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Buscar imagen por ID existente")
    void testFindById() {
        Optional<Imagen> encontrada = imagenRepository.findById(imagen.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre()).isEqualTo("principal_Evento Test");
        assertThat(encontrada.get().getTipo()).isEqualTo(1);
        assertThat(encontrada.get().getEvento().getId()).isEqualTo(evento.getId());
    }

    @Test
    @DisplayName("Buscar imagen por ID no existente")
    void testFindByIdNotFound() {
        Optional<Imagen> noEncontrada = imagenRepository.findById(999L);
        assertThat(noEncontrada).isEmpty();
    }

    @Test
    @DisplayName("Buscar todas las imágenes")
    void testFindAll() {
        // Crear otra imagen para el mismo evento
        Imagen imagen2 = new Imagen("banner-image.jpg", 2, evento);
        testEntityManager.persist(imagen2);
        testEntityManager.flush();

        List<Imagen> imagenes = imagenRepository.findAll();

        assertThat(imagenes).isNotEmpty();
        assertThat(imagenes).hasSize(2);
        assertThat(imagenes).extracting(Imagen::getTipo).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Buscar imágenes por evento ID")
    void testFindByEventoId() {
        // Crear otro evento
        Evento otroEvento = Evento.builder()
                .pulep("EV-2025-002")
                .artistas("Otro Artista")
                .nombre("Otro Evento")
                .fechaApertura(LocalDateTime.of(2025, 7, 1, 10, 0))
                .estado(2)
                .temporada(temporada)
                .tipo(tipo)
                .build();
        testEntityManager.persist(otroEvento);

        // Crear imagen para el nuevo evento
        Imagen imagenOtroEvento = new Imagen("other-image.jpg", 3, otroEvento);
        testEntityManager.persist(imagenOtroEvento);
        testEntityManager.flush();

        List<Imagen> imagenes = imagenRepository.findByEventoId(evento.getId());

        assertThat(imagenes).isNotEmpty();
        assertThat(imagenes).hasSize(1);
        assertThat(imagenes.get(0).getEvento().getId()).isEqualTo(evento.getId());
    }

    @Test
    @DisplayName("Buscar imágenes por tipo")
    void testFindByTipo() {
        // Crear imágenes de diferentes tipos
        Imagen imagenBanner = new Imagen("banner.jpg", 2, evento);
        Imagen imagenQR = new Imagen("qr.jpg", 3, evento);
        Imagen imagenPrincipal2 = new Imagen("principal2.jpg", 1, evento);

        testEntityManager.persist(imagenBanner);
        testEntityManager.persist(imagenQR);
        testEntityManager.persist(imagenPrincipal2);
        testEntityManager.flush();

        List<Imagen> imagenesPrincipales = imagenRepository.findByTipo(1);

        assertThat(imagenesPrincipales).hasSize(2);
        assertThat(imagenesPrincipales).allMatch(img -> img.getTipo() == 1);
    }

    @Test
    @DisplayName("Buscar imágenes por evento ID y tipo")
    void testFindByEventoIdAndTipo() {
        // Crear imágenes de diferentes tipos para el mismo evento
        Imagen imagenBanner = new Imagen("banner.jpg", 2, evento);
        Imagen imagenQR = new Imagen("qr.jpg", 3, evento);

        testEntityManager.persist(imagenBanner);
        testEntityManager.persist(imagenQR);
        testEntityManager.flush();

        List<Imagen> imagenesBanner = imagenRepository.findByEventoIdAndTipo(evento.getId(), 2);

        assertThat(imagenesBanner).hasSize(1);
        assertThat(imagenesBanner.get(0).getTipo()).isEqualTo(2);
        assertThat(imagenesBanner.get(0).getEvento().getId()).isEqualTo(evento.getId());
    }

    @Test
    @DisplayName("Editar una imagen existente")
    void testEditarImagen() {
        Optional<Imagen> encontrada = imagenRepository.findById(imagen.getId());
        assertThat(encontrada).isPresent();

        Imagen imagenAEditar = encontrada.get();
        imagenAEditar.setUrl("https://marcablanca.allticketscol.com/nueva-imagen.jpg");
        imagenRepository.save(imagenAEditar);

        Imagen actualizada = imagenRepository.findById(imagen.getId()).orElse(null);
        assertThat(actualizada).isNotNull();
        assertThat(actualizada.getUrl()).isEqualTo("https://marcablanca.allticketscol.com/nueva-imagen.jpg");
    }

    @Test
    @DisplayName("Eliminar una imagen existente")
    void testEliminarImagen() {
        Long id = imagen.getId();
        imagenRepository.deleteById(id);

        Optional<Imagen> eliminada = imagenRepository.findById(id);
        assertThat(eliminada).isEmpty();
    }

    @Test
    @DisplayName("Eliminar imagen inexistente - No debe fallar")
    void testDeleteImagenInexistente() {
        Long idInexistente = 999L;
        long countAntes = imagenRepository.count();

        imagenRepository.deleteById(idInexistente);

        long countDespues = imagenRepository.count();
        assertThat(countDespues).isEqualTo(countAntes);
    }


}
