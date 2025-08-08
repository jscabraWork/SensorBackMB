package com.arquitectura.evento;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.tipo.entity.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class EventoRepositoryTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Evento evento;
    private SimpleDateFormat dateFormat;
    private Temporada temporada;
    private Tipo tipo;

    @BeforeEach
    void setup() throws ParseException {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaApertura = dateFormat.parse("2025-06-01");

        temporada = Temporada.builder().nombre("Temporada Test").build();
        tipo = Tipo.builder().nombre("Concierto").build();

        testEntityManager.persist(temporada);
        testEntityManager.persist(tipo);
        testEntityManager.flush();

        evento = Evento.builder()
                .pulep("EV-2025-001")
                .artistas("Artista Principal, Artista Invitado")
                .nombre("Gran Concierto")
                .recomendaciones("Llegar 1 hora antes")
                .video("https://youtube.com/embed/video123")
                .fechaApertura(LocalDateTime.of(2025, 6, 1, 10, 0))
                .estado(2)
                .temporada(temporada)
                .tipo(tipo)
                .build();

        testEntityManager.persist(evento);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Buscar evento por ID existente")
    void testFindById() {
        Evento encontrado = eventoRepository.findById(evento.getId()).orElse(null);
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Gran Concierto");
    }

    @Test
    @DisplayName("Buscar evento por ID no existente")
    void testFindByIdNotFound() {
        Evento noEncontrado = eventoRepository.findById(999L).orElse(null);
        assertThat(noEncontrado).isNull();
    }

    @Test
    @DisplayName("Buscar eventos por estado y temporada")
    void testFindAllByEstadoAndTemporada() {
        // Crear otro evento con diferente estado para probar el filtrado
        Evento eventoInactivo = Evento.builder()
                .pulep("EV-2025-002")
                .artistas("Otro Artista")
                .nombre("Evento Inactivo")
                .recomendaciones("Sin recomendaciones")
                .video("https://youtube.com/embed/video456")
                .fechaApertura(evento.getFechaApertura())
                .estado(1) // Estado diferente
                .temporada(temporada) // Misma temporada
                .tipo(tipo)
                .build();

        testEntityManager.persist(eventoInactivo);
        testEntityManager.flush();

        // 1. Buscar eventos activos (estado=2) para la temporada
        List<Evento> eventosActivos = eventoRepository.findAllByEstadoAndTemporadaId(2, temporada.getId());

        assertThat(eventosActivos)
                .isNotEmpty()
                .hasSize(1) // Solo el evento original tiene estado=2
                .allSatisfy(e -> {
                    assertThat(e.getEstado()).isEqualTo(2);
                    assertThat(e.getTemporada().getId()).isEqualTo(temporada.getId());
                });

        // 2. Buscar eventos inactivos (estado=1) para la temporada
        List<Evento> eventosInactivos = eventoRepository.findAllByEstadoAndTemporadaId(1, temporada.getId());

        assertThat(eventosInactivos)
                .isNotEmpty()
                .hasSize(1) // Solo el nuevo evento tiene estado=1
                .allSatisfy(e -> {
                    assertThat(e.getEstado()).isEqualTo(1);
                    assertThat(e.getTemporada().getId()).isEqualTo(temporada.getId());
                });

        // 3. Buscar con combinación que no debería devolver resultados
        List<Evento> sinResultados = eventoRepository.findAllByEstadoAndTemporadaId(2, 999L);
        assertThat(sinResultados).isEmpty();
    }


    @Test
    @DisplayName("Editar un evento existente")
    void testEditarEvento() {
        Evento encontrado = eventoRepository.findById(evento.getId()).orElse(null);
        assertThat(encontrado).isNotNull();

        encontrado.setNombre("Concierto Editado");
        eventoRepository.save(encontrado);

        Evento actualizado = eventoRepository.findById(evento.getId()).orElse(null);
        assertThat(actualizado.getNombre()).isEqualTo("Concierto Editado");
    }

    @Test
    @DisplayName("Eliminar un evento existente")
    void testEliminarEvento() {
        Long id = evento.getId();
        eventoRepository.deleteById(id);

        Evento eliminado = eventoRepository.findById(id).orElse(null);
        assertThat(eliminado).isNull();
    }

    @Test
    @DisplayName("Eliminar evento inexistente - No debe fallar")
    void testDeleteEventoInexistente() {
        Long idInexistente = 999L;
        eventoRepository.deleteById(idInexistente);
        assertThat(eventoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Trae eventos por su id y por una lista de estados - Falla")
    void testEventoPorIdYestadosNot() {
        Evento eventoNoValido = new Evento();
        eventoNoValido.setId(2L);
        eventoNoValido.setEstado(3);

        List<Integer> estadosEsperados = Arrays.asList(1, 2);

        Evento resultado = eventoRepository.findByIdAndEstadoIn(2L, estadosEsperados);

        assertThat(resultado).isNull();
    }


    @Test
    @DisplayName("Trae eventos por su id y por una lista de estados - Exitoso")
    void testEventoPorIdYestadosExitoso() {
        List<Integer> estadosEsperados = Arrays.asList(1, 2);

        Evento encontrado = eventoRepository.findByIdAndEstadoIn(evento.getId(), estadosEsperados);

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getId()).isEqualTo(evento.getId());
        assertThat(encontrado.getEstado()).isEqualTo(2);
    }


}
