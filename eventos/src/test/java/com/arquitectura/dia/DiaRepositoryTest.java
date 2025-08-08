package com.arquitectura.dia;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class DiaRepositoryTest {

    @Autowired
    private DiaRepository diaRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Dia dia;

    @BeforeEach
    void setup() {
        dia = Dia.builder()
                .nombre("Día de prueba")
                .estado(1)
                .fechaInicio(LocalDateTime.now())
                .fechaFin(LocalDateTime.now().plusDays(1))
                .horaInicio("09:00")
                .horaFin("18:00")
                .evento(null)
                .build();

        testEntityManager.persist(dia);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Buscar día por ID existente")
    void testFindById() {
        Dia encontrado = diaRepository.findById(dia.getId()).orElse(null);

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Día de prueba");
    }

    @Test
    @DisplayName("Buscar día por ID no existente")
    void testFindByIdNotFound() {
        Dia noEncontrado = diaRepository.findById(999L).orElse(null);

        assertThat(noEncontrado).isNull();
    }

    @Test
    @DisplayName("Buscar todos los días por estado y evento ID")
    void testFindAllByEstadoAndEventoId() {
        // Primero persistir las entidades relacionadas
        Temporada temporada = testEntityManager.persistFlushFind(
                Temporada.builder().nombre("Temporada Test").build()
        );

        Tipo tipo = testEntityManager.persistFlushFind(
                Tipo.builder().nombre("Concierto").build()
        );

        // Persistir el evento y obtener la versión gestionada
        Evento evento = testEntityManager.persistFlushFind(
                Evento.builder()
                        .pulep("EV-2025-001")
                        .artistas("Artista Principal, Artista Invitado")
                        .nombre("Gran Concierto")
                        .recomendaciones("Llegar 1 hora antes")
                        .video("https://youtube.com/embed/video123")
                        .fechaApertura(LocalDateTime.of(2025, 6, 1, 10, 0))
                        .estado(2)
                        .temporada(temporada)
                        .tipo(tipo)
                        .build()
        );

        // Crear y persistir los días usando el evento gestionado
        Dia diaActivo = testEntityManager.persistFlushFind(
                Dia.builder()
                        .nombre("Día activo")
                        .estado(1)
                        .fechaInicio(LocalDateTime.now())
                        .fechaFin(LocalDateTime.now().plusDays(1))
                        .horaInicio("09:00")
                        .horaFin("18:00")
                        .evento(evento)
                        .build()
        );

        Dia diaInactivo = testEntityManager.persistFlushFind(
                Dia.builder()
                        .nombre("Día inactivo")
                        .estado(0)
                        .fechaInicio(LocalDateTime.now())
                        .fechaFin(LocalDateTime.now().plusDays(1))
                        .horaInicio("10:00")
                        .horaFin("19:00")
                        .evento(evento)
                        .build()
        );

        // Buscar los días activos para un evento específico
        List<Dia> diasActivos = diaRepository.findAllByEstadoAndEventoId(1, evento.getId());
        assertThat(diasActivos).hasSize(1);
        assertThat(diasActivos.get(0).getEstado()).isEqualTo(1);
        assertThat(diasActivos.get(0).getEvento().getId()).isEqualTo(evento.getId());

        // Buscar los días inactivos para el mismo evento
        List<Dia> diasInactivos = diaRepository.findAllByEstadoAndEventoId(0, evento.getId());
        assertThat(diasInactivos).hasSize(1);
        assertThat(diasInactivos.get(0).getEstado()).isEqualTo(0);
        assertThat(diasInactivos.get(0).getEvento().getId()).isEqualTo(evento.getId());
    }


    @Test
    @DisplayName("Guardar un nuevo día")
    void testSave() {
        Dia nuevoDia = Dia.builder()
                .nombre("Nuevo día")
                .estado(1)
                .fechaInicio(LocalDateTime.now())
                .fechaFin(LocalDateTime.now().plusDays(1))
                .horaInicio("08:00")
                .horaFin("17:00")
                .build();

        Dia guardado = diaRepository.save(nuevoDia);
        assertThat(guardado.getId()).isNotNull();
        assertThat(diaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Actualizar un día existente")
    void testUpdate() {
        Dia encontrado = diaRepository.findById(dia.getId()).orElse(null);
        assertThat(encontrado).isNotNull();

        encontrado.setNombre("Día actualizado");
        encontrado.setHoraInicio("10:00");
        diaRepository.save(encontrado);

        Dia actualizado = diaRepository.findById(dia.getId()).orElse(null);
        assertThat(actualizado.getNombre()).isEqualTo("Día actualizado");
        assertThat(actualizado.getHoraInicio()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("Eliminar un día existente")
    void testDelete() {
        Long id = dia.getId();
        diaRepository.deleteById(id);

        Dia eliminado = diaRepository.findById(id).orElse(null);
        assertThat(eliminado).isNull();
    }

    @Test
    @DisplayName("Buscar día con localidades - Relación cargada")
    void testFindDiaWithLocalidades() {
        // Arrange
        Localidad localidad = new Localidad();
        localidad.setNombre("Localidad 1");
        localidad.setDias(List.of(dia));
        testEntityManager.persist(localidad);

        dia.setLocalidades(List.of(localidad));
        testEntityManager.persistAndFlush(dia);
        testEntityManager.clear();

        // Act
        Dia encontrado = diaRepository.findById(dia.getId())
                .orElseThrow();

        // Assert
        assertThat(encontrado.getLocalidades()).hasSize(1);
        assertThat(encontrado.getLocalidades().get(0).getNombre()).isEqualTo("Localidad 1");
    }
}
