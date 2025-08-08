package com.arquitectura.localidad;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
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
public class LocalidadRepositoryTest {

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Localidad localidad;

    @BeforeEach
    void setup() {
        localidad = Localidad.builder()
                .nombre("Localidad de prueba")
                .build();

        testEntityManager.persist(localidad);
        testEntityManager.flush();
    }

    // Test CRUD básicos
    @Test
    @DisplayName("Buscar localidad por ID existente")
    void testFindById() {
        Localidad encontrada = localidadRepository.findById(localidad.getId()).orElse(null);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getNombre()).isEqualTo("Localidad de prueba");
    }

    @Test
    @DisplayName("Buscar localidad por ID no existente")
    void testFindByIdNotFound() {
        Localidad noEncontrada = localidadRepository.findById(999L).orElse(null);

        assertThat(noEncontrada).isNull();
    }

    @Test
    @DisplayName("Guardar una nueva localidad")
    void testSave() {
        Localidad nuevaLocalidad = Localidad.builder()
                .nombre("Nueva Localidad")
                .build();

        Localidad guardada = localidadRepository.save(nuevaLocalidad);

        assertThat(guardada.getId()).isNotNull();
        assertThat(localidadRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Actualizar una localidad existente")
    void testUpdate() {
        Localidad encontrada = localidadRepository.findById(localidad.getId()).orElse(null);
        assertThat(encontrada).isNotNull();

        encontrada.setNombre("Localidad actualizada");
        localidadRepository.save(encontrada);

        Localidad actualizada = localidadRepository.findById(localidad.getId()).orElse(null);
        assertThat(actualizada.getNombre()).isEqualTo("Localidad actualizada");
    }

    @Test
    @DisplayName("Eliminar una localidad existente")
    void testDelete() {
        Long id = localidad.getId();
        localidadRepository.deleteById(id);

        Localidad eliminada = localidadRepository.findById(id).orElse(null);
        assertThat(eliminada).isNull();
    }


    @Test
    @DisplayName("Buscar localidades por nombre (case insensitive)")
    void testFindAllByNombreIgnoreCase() {
        // Crear otra localidad con mismo nombre pero diferente capitalización
        Localidad localidad2 = testEntityManager.persistFlushFind(
                Localidad.builder()
                        .nombre("LOCALIDAD de prueba")
                        .build()
        );

        List<Localidad> encontradas = localidadRepository.findAllByNombreIgnoreCase("localidad DE prueba");

        assertThat(encontradas).hasSize(2);
        assertThat(encontradas).extracting(Localidad::getId)
                .containsExactlyInAnyOrder(localidad.getId(), localidad2.getId());
    }

    @Test
    @DisplayName("Buscar localidades por nombre excluyendo ID")
    void testFindAllByNombreIgnoreCaseAndIdNot() {
        Localidad localidad2 = testEntityManager.persistFlushFind(
                Localidad.builder()
                        .nombre("Localidad de prueba")
                        .build()
        );

        List<Localidad> encontradas = localidadRepository.findAllByNombreIgnoreCaseAndIdNot(
                "localidad DE prueba",
                localidad.getId()
        );

        assertThat(encontradas).hasSize(1);
        assertThat(encontradas.get(0).getId()).isEqualTo(localidad2.getId());
    }

    @Test
    @DisplayName("Buscar localidad con días asociados - Relación cargada")
    void testFindLocalidadWithDias() {
        // 1. Crear día de prueba con la colección de localidades inicializada
        Dia dia = Dia.builder()
                .nombre("Día asociado")
                .estado(1)
                .fechaInicio(LocalDateTime.of(2024, 6, 1, 0, 0))
                .fechaFin(LocalDateTime.of(2024, 6, 1, 0, 0))
                .horaInicio("09:00")
                .horaFin("18:00")
                .localidades(new ArrayList<>()) // Inicializar colección
                .build();

        // 2. Persistir el día primero
        Dia diaPersistido = testEntityManager.persistFlushFind(dia);

        // 3. Asegurar que la localidad tiene la colección de días inicializada
        if (localidad.getDias() == null) {
            localidad.setDias(new ArrayList<>());
        }

        // 4. Establecer relación bidireccional
        localidad.getDias().add(diaPersistido);
        diaPersistido.getLocalidades().add(localidad);

        // 5. Persistir ambos lados de la relación
        testEntityManager.persistAndFlush(localidad);
        testEntityManager.persistAndFlush(diaPersistido);
        testEntityManager.clear(); // Limpiar la caché

        // 6. Buscar la localidad
        Localidad encontrada = localidadRepository.findById(localidad.getId())
                .orElseThrow();

        // 7. Verificaciones
        assertThat(encontrada.getDias())
                .hasSize(1)
                .extracting(Dia::getNombre)
                .containsExactly("Día asociado");

        // Verificación adicional de la relación inversa
        Dia diaRecuperado = testEntityManager.find(Dia.class, diaPersistido.getId());
        assertThat(diaRecuperado.getLocalidades())
                .hasSize(1)
                .extracting(Localidad::getNombre)
                .containsExactly("Localidad de prueba");
    }





}
