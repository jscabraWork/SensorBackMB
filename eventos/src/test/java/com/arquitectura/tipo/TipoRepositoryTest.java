package com.arquitectura.tipo;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.entity.TipoRepository;

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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class TipoRepositoryTest {

    @Autowired
    private TipoRepository tipoRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Tipo tipo;

    @BeforeEach
    void setup() {
        tipo = Tipo.builder()
                .nombre("Tipo de Prueba")
                .build();

        testEntityManager.persist(tipo);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Buscar tipo por ID existente")
    void testFindById() {
        Tipo encontrado = tipoRepository.findById(tipo.getId()).orElse(null);

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Tipo de Prueba");
    }

    @Test
    @DisplayName("Buscar tipo por ID no existente")
    void testFindByIdNotFound() {
        Tipo noEncontrado = tipoRepository.findById(999L).orElse(null);

        assertThat(noEncontrado).isNull();
    }

    @Test
    @DisplayName("Guardar un nuevo tipo")
    void testSaveTipo() {
        Tipo nuevoTipo = Tipo.builder()
                .nombre("Nuevo Tipo")
                .build();

        Tipo guardado = tipoRepository.save(nuevoTipo);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Nuevo Tipo");
    }

    @Test
    @DisplayName("Editar un tipo existente")
    void testEditarTipo() {
        Tipo encontrado = tipoRepository.findById(tipo.getId()).orElse(null);
        assertThat(encontrado).isNotNull();

        encontrado.setNombre("Tipo Editado");
        tipoRepository.save(encontrado);

        Tipo actualizado = tipoRepository.findById(tipo.getId()).orElse(null);
        assertThat(actualizado.getNombre()).isEqualTo("Tipo Editado");
    }

    @Test
    @DisplayName("Eliminar un tipo existente")
    void testEliminarTipo() {
        Long id = tipo.getId();
        tipoRepository.deleteById(id);

        Tipo eliminado = tipoRepository.findById(id).orElse(null);
        assertThat(eliminado).isNull();
    }

    @Test
    @DisplayName("Listar todos los tipos")
    void testFindAll() {
        List<Tipo> tipos = tipoRepository.findAll();

        assertThat(tipos).isNotEmpty();
        assertThat(tipos).hasSize(1);
        assertThat(tipos.get(0).getNombre()).isEqualTo("Tipo de Prueba");
    }

    @Test
    @DisplayName("Contar tipos existentes")
    void testCount() {
        long count = tipoRepository.count();

        assertThat(count).isEqualTo(1);
    }
}