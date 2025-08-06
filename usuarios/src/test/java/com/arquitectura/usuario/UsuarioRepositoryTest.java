package com.arquitectura.usuario;

import com.arquitectura.UsuariosApplication;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.entity.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = UsuariosApplication.class)
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Usuario usuario;
    private Role roleAdmin;
    private Role roleOrg;

    @BeforeEach
    void setup() {
        roleAdmin = new Role();
        roleAdmin.setNombre("ROLE_ADMIN");
        testEntityManager.persist(roleAdmin);

        roleOrg = new Role();
        roleOrg.setNombre("ROLE_ORGANIZADOR");
        testEntityManager.persist(roleOrg);

        usuario = Usuario.builder()
                .numeroDocumento("12345678")
                .nombre("Usuario Prueba")
                .correo("usuario@test.com")
                .celular("3001234567")
                .roles(Arrays.asList(roleAdmin, roleOrg))
                .build();

        testEntityManager.persist(usuario);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Buscar usuario por correo")
    void testFindByCorreo() {
        Usuario encontrado = usuarioRepository.findByCorreo("usuario@test.com");
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNumeroDocumento()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Buscar usuario por rol")
    void testFindByRolesNombre() {
        List<Usuario> usuarios = usuarioRepository.findByRolesNombre("ROLE_ADMIN");
        assertThat(usuarios).isNotEmpty();
        assertThat(usuarios.get(0).getCorreo()).isEqualTo("usuario@test.com");
    }

    @Test
    @DisplayName("Buscar usuario por correo y roles (fetch)")
    void testFindByCorreoByRoles() {
        Usuario encontrado = usuarioRepository.findByCorreoByRoles("usuario@test.com");
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getRoles()).extracting(Role::getNombre).contains("ROLE_ADMIN", "ROLE_ORGANIZADOR");
    }

    @Test
    @DisplayName("Buscar usuario pre-registro (por documento, correo o celular)")
    void testBuscarPreRegistro() {
        Usuario encontrado = usuarioRepository.buscarPreRegistro("12345678", "usuario@test.com", "3001234567");
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getCorreo()).isEqualTo("usuario@test.com");
    }

    @Test
    @DisplayName("Validar correos (por documento, correo o celular)")
    void testValidarCorreos() {
        List<Usuario> encontrados = usuarioRepository.validarCorreos("12345678", "usuario@test.com", "3001234567");
        assertThat(encontrados).isNotEmpty();
        assertThat(encontrados.get(0).getNumeroDocumento()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Buscar usuarios paginados por rol")
    void testFindByRolesId() {
        Page<Usuario> page = usuarioRepository.findByRolesId(roleAdmin.getId(), PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getCorreo()).isEqualTo("usuario@test.com");
    }

    @Test
    @DisplayName("No encuentra usuario por correo inexistente")
    void testFindByCorreoNotFound() {
        Usuario noEncontrado = usuarioRepository.findByCorreo("noexiste@test.com");
        assertThat(noEncontrado).isNull();
    }

    @Test
    @DisplayName("No encuentra usuario por rol inexistente")
    void testFindByRolesNombreNotFound() {
        List<Usuario> usuarios = usuarioRepository.findByRolesNombre("ROLE_INEXISTENTE");
        assertThat(usuarios).isEmpty();
    }

    @Test
    @DisplayName("Guardar nuevo usuario")
    void testSaveUsuario() {
        Usuario nuevoUsuario = Usuario.builder()
                .numeroDocumento("87654321")
                .nombre("Nuevo Usuario")
                .correo("nuevo@test.com")
                .celular("3107654321")
                .roles(Collections.singletonList(roleOrg))
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getNumeroDocumento()).isEqualTo("87654321");
    }

    @Test
    @DisplayName("Modificar usuario existente")
    void testUpdateUsuario() {
        // Primero limpia el persistence context
        testEntityManager.clear();

        // Recupera el usuario fresco de la base de datos
        Usuario usuarioToUpdate = usuarioRepository.findById(usuario.getNumeroDocumento()).orElseThrow();

        // Realiza las modificaciones
        usuarioToUpdate.setNombre("Usuario Modificado");
        usuarioToUpdate.setCorreo("modificado@test.com");

        // Guarda y vuelve a cargar
        usuarioRepository.saveAndFlush(usuarioToUpdate);
        testEntityManager.clear();

        // Verifica
        Usuario modificado = usuarioRepository.findById(usuario.getNumeroDocumento()).orElseThrow();
        assertThat(modificado.getNombre()).isEqualTo("Usuario Modificado");
        assertThat(modificado.getCorreo()).isEqualTo("modificado@test.com");
    }

    @Test
    @DisplayName("Eliminar usuario")
    void testDeleteUsuario() {
        String numeroDocumento = usuario.getNumeroDocumento();

        usuarioRepository.delete(usuario);

        Usuario eliminado = usuarioRepository.findByCorreo("usuario@test.com");
        assertThat(eliminado).isNull();

        List<Usuario> encontrados = usuarioRepository.validarCorreos(numeroDocumento, "usuario@test.com", "3001234567");
        assertThat(encontrados).isEmpty();
    }
}
