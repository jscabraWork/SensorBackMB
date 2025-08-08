package com.arquitectura.tarifa;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.temporada.entity.Temporada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
class TarifaRepositoryTest {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TestEntityManager em;

    private Tarifa tarifa;
    private Localidad localidad;
    private Evento evento;

    @BeforeEach
    void setUp() {
        // Crear Temporada dummy
        Temporada temporada = Temporada.builder()
                .nombre("Temporada 1")
                .estado(0)
                .build();
        em.persist(temporada);

        // Crear Evento
        evento = Evento.builder()
                .nombre("Concierto")
                .pulep("ABC123")
                .artistas("Artista X")
                .fechaApertura(LocalDateTime.now())
                .estado(2)
                .temporada(temporada)
                .build();
        em.persist(evento);

        // Crear Localidad
        localidad = Localidad.builder()
                .nombre("VIP")
                .build();
        em.persist(localidad);

        // Crear Tarifa
        tarifa = Tarifa.builder()
                .nombre("Tarifa VIP")
                .precio(100.0)
                .iva(10.0)
                .servicio(5.0)
                .estado(1)
                .localidad(localidad)
                .build();
        em.persist(tarifa);

        // Asociar tarifa a localidad
        localidad.setTarifas(new ArrayList<>(List.of(tarifa)));
        em.persist(localidad);

        // Crear Día
        Dia dia = Dia.builder()
                .nombre("Día 1")
                .estado(0)
                .fechaInicio(LocalDateTime.now())
                .fechaFin(LocalDateTime.now().plusDays(1))
                .horaInicio("10:00")
                .horaFin("22:00")
                .evento(evento)
                .localidades(List.of(localidad))
                .build();
        em.persist(dia);

        em.flush();
    }

    @Test
    @DisplayName("Guardar una nueva tarifa")
    void testSaveTarifa() {
        Tarifa nuevaTarifa = Tarifa.builder()
                .nombre("Tarifa General")
                .precio(80.0)
                .iva(8.0)
                .servicio(4.0)
                .estado(1)
                .localidad(localidad)
                .build();

        Tarifa guardada = tarifaRepository.save(nuevaTarifa);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Tarifa General");
    }

    @Test
    @DisplayName("Actualizar una tarifa existente")
    void testUpdateTarifa() {
        Tarifa encontrada = tarifaRepository.findById(tarifa.getId()).orElse(null);
        assertThat(encontrada).isNotNull();

        encontrada.setPrecio(150.0);
        encontrada.setNombre("Tarifa Actualizada");

        tarifaRepository.save(encontrada);

        Tarifa actualizada = tarifaRepository.findById(tarifa.getId()).orElse(null);
        assertThat(actualizada).isNotNull();
        assertThat(actualizada.getNombre()).isEqualTo("Tarifa Actualizada");
        assertThat(actualizada.getPrecio()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Eliminar una tarifa existente")
    void testDeleteTarifa() {
        Long id = tarifa.getId();
        tarifaRepository.deleteById(id);

        Tarifa eliminada = tarifaRepository.findById(id).orElse(null);
        assertThat(eliminada).isNull();
    }

    @Test
    @DisplayName("Debe encontrar tarifas por estado y localidad ID")
    void testFindAllByEstadoAndLocalidadesId() {
        List<Tarifa> result = tarifaRepository.findAllByEstadoAndLocalidadId(1, localidad.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tarifa VIP");
    }

    @Test
    @DisplayName("Debe encontrar tarifas por evento ID (query nativa)")
    void testFindAllByEventoId() {
        List<Tarifa> result = tarifaRepository.findAllByEventoId(evento.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tarifa VIP");
    }
}
