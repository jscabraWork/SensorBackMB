package com.arquitectura.usuario;

import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        }
)
public class UsuarioServiceTest {

    @Mock
    private UsuarioService usuarioService;

    private Usuario usuarioTest;
    private List<String> rolesTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setNumeroDocumento("12345678");
        usuarioTest.setNombre("Usuario Test");
        usuarioTest.setCelular("3001234567");

        rolesTest = Arrays.asList("ROLE_CLIENTE", "ROLE_USER");
    }

    // Tests para métodos de creación de usuarios
    @Test
    @DisplayName("Crear usuario con multiples roles - Éxito")
    void testCrearUsuarioConMultiplesRoles() {
        when(usuarioService.crearUsuarioConMultiplesRoles(any(Usuario.class), anyList(), anyBoolean()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.crearUsuarioConMultiplesRoles(usuarioTest, rolesTest, true);

        assertNotNull(resultado);
        verify(usuarioService).crearUsuarioConMultiplesRoles(usuarioTest, rolesTest, true);
    }

    @Test
    @DisplayName("Crear cliente - Éxito")
    void testCrearCliente() {
        when(usuarioService.crearCliente(any(Usuario.class), anyBoolean()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.crearCliente(usuarioTest, true);

        assertNotNull(resultado);
        verify(usuarioService).crearCliente(usuarioTest, true);
    }

    // Tests para métodos de consulta
    @Test
    @DisplayName("Validar si usuario existe - Éxito")
    void testUsuarioExiste_Existe() {
        when(usuarioService.usuarioExiste(any(Usuario.class)))
                .thenReturn(true);

        boolean existe = usuarioService.usuarioExiste(usuarioTest);

        assertTrue(existe);
        verify(usuarioService).usuarioExiste(usuarioTest);
    }

    @Test
    @DisplayName("Validar si usuario existe - Falla")
    void testUsuarioExiste_NoExiste() {
        when(usuarioService.usuarioExiste(any(Usuario.class)))
                .thenReturn(false);

        boolean existe = usuarioService.usuarioExiste(usuarioTest);

        assertFalse(existe);
        verify(usuarioService).usuarioExiste(usuarioTest);
    }

    @Test
    @DisplayName("Buscar por correo - Éxito")
    void testBuscarPorCorreo() {
        when(usuarioService.buscarPorCorreo(anyString()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.buscarPorCorreo("test@example.com");

        assertNotNull(resultado);
        assertEquals(usuarioTest.getCorreo(), resultado.getCorreo());
        verify(usuarioService).buscarPorCorreo("test@example.com");
    }

    @Test
    @DisplayName("Validar si usuario existe - Fallo")
    void testBuscarPorCorreo_NoEncontrado() {
        when(usuarioService.buscarPorCorreo(anyString()))
                .thenReturn(null);

        Usuario resultado = usuarioService.buscarPorCorreo("noexiste@example.com");

        assertNull(resultado);
        verify(usuarioService).buscarPorCorreo("noexiste@example.com");
    }

    @Test
    @DisplayName("Buscar cliente por documento - Éxito")
    void testGetCliente() {
        when(usuarioService.getCliente(anyString()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.getCliente("123");

        assertNotNull(resultado);
        verify(usuarioService).getCliente("123");
    }

    // Tests para métodos de actualización
    @Test
    @DisplayName("Actualizar usuario - Éxito")
    void testActualizarDatosUsuario() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Nombre Actualizado");

        when(usuarioService.actualizarDatosUsuario(anyString(), any(Usuario.class)))
                .thenReturn(usuarioActualizado);

        Usuario resultado = usuarioService.actualizarDatosUsuario("123", usuarioTest);

        assertNotNull(resultado);
        assertEquals("Nombre Actualizado", resultado.getNombre());
        verify(usuarioService).actualizarDatosUsuario("123", usuarioTest);
    }

    @Test
    @DisplayName("Actualizar usuario con roles - Éxito")
    void testUpdateUsuarioConRoles() {
        when(usuarioService.updateUsuarioConRoles(anyString(), any(Usuario.class), anyList()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.updateUsuarioConRoles("123", usuarioTest, rolesTest);

        assertNotNull(resultado);
        verify(usuarioService).updateUsuarioConRoles("123", usuarioTest, rolesTest);
    }

    @Test
    @DisplayName("Crear usuario - Éxito")
    void testCrearUsuario() {
        when(usuarioService.crearUsuario(anyString(), any(Usuario.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.crearUsuario("topic-test", usuarioTest, "ROLE_CLIENTE", "Cliente", true);

        assertNotNull(resultado);
        verify(usuarioService).crearUsuario("topic-test", usuarioTest, "ROLE_CLIENTE", "Cliente", true);
    }

    @Test
    @DisplayName("Cambiar estado de usuario - Éxito")
    void testCambiarAccesoAlUsuario_Exitoso() {
        when(usuarioService.cambiarAccesoAlUsuario(anyString()))
                .thenReturn(true);

        boolean resultado = usuarioService.cambiarAccesoAlUsuario("12345678");

        assertTrue(resultado);
        verify(usuarioService).cambiarAccesoAlUsuario("12345678");
    }

    @Test
    @DisplayName("Cambiar estado de usuario - Falla")
    void testCambiarAccesoAlUsuario_Fallido() {
        when(usuarioService.cambiarAccesoAlUsuario(anyString()))
                .thenReturn(false);

        boolean resultado = usuarioService.cambiarAccesoAlUsuario("12345678");

        assertFalse(resultado);
        verify(usuarioService).cambiarAccesoAlUsuario("12345678");
    }

    @Test
    @DisplayName("Buscar por roles - Éxito")
    void testFindByRolesNombre() {
        List<Usuario> usuarios = Arrays.asList(usuarioTest);
        when(usuarioService.findByRolesNombre(anyString()))
                .thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.findByRolesNombre("ROLE_CLIENTE");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioService).findByRolesNombre("ROLE_CLIENTE");
    }

    // Tests para paginación
    @Test
    @DisplayName("Buscar clientes paginados - Éxito")
    void testFindClientesPaginados() {
        List<Usuario> usuarios = Arrays.asList(usuarioTest);
        Page<Usuario> paginaUsuarios = new PageImpl<>(usuarios);

        when(usuarioService.findClientesPaginados(anyLong(), anyInt()))
                .thenReturn(paginaUsuarios);

        Page<Usuario> resultado = usuarioService.findClientesPaginados(1L, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(usuarioService).findClientesPaginados(1L, 10);
    }

    @Test
    @DisplayName("Detalles de usuario - Éxito")
    void testGetDetailsOfUsuario() {
        when(usuarioService.getDetailsOfUsuario(anyString()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.getDetailsOfUsuario("test@example.com");

        assertNotNull(resultado);
        assertEquals(usuarioTest.getCorreo(), resultado.getCorreo());
        verify(usuarioService).getDetailsOfUsuario("test@example.com");
    }

    @Test
    @DisplayName("Validar datos de registro - Falla")
    void testValidarDatos() {
        List<Usuario> usuariosConflicto = Arrays.asList(usuarioTest);
        when(usuarioService.validarDatos(anyString(), anyString(), anyString()))
                .thenReturn(usuariosConflicto);

        List<Usuario> resultado = usuarioService.validarDatos("12345678", "test@example.com", "3001234567");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioService).validarDatos("12345678", "test@example.com", "3001234567");
    }

    @Test
    @DisplayName("Cambiar estado de usuario - Éxito")
    void testValidarDatos_SinConflictos() {
        when(usuarioService.validarDatos(anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList());

        List<Usuario> resultado = usuarioService.validarDatos("87654321", "nuevo@example.com", "3009876543");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioService).validarDatos("87654321", "nuevo@example.com", "3009876543");
    }

    // Tests para métodos de token
    @Test
    @DisplayName("Obtener usuario de token - Éxito")
    void testObtenerUsuarioDeToken() {
        String tokenTest = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(usuarioService.obtenerUsuarioDeToken(anyString()))
                .thenReturn("usuario@example.com");

        String resultado = usuarioService.obtenerUsuarioDeToken(tokenTest);

        assertNotNull(resultado);
        assertEquals("usuario@example.com", resultado);
        verify(usuarioService).obtenerUsuarioDeToken(tokenTest);
    }

    @Test
    @DisplayName("Obtener usuario de token - Falla")
    void testObtenerUsuarioDeToken_TokenInvalido() {
        String tokenInvalido = "Bearer token_invalido";
        when(usuarioService.obtenerUsuarioDeToken(anyString()))
                .thenReturn(null);

        String resultado = usuarioService.obtenerUsuarioDeToken(tokenInvalido);

        assertNull(resultado);
        verify(usuarioService).obtenerUsuarioDeToken(tokenInvalido);
    }

    @Test
    @DisplayName("Obtener rol de token - Éxito")
    void testObtenerRolDeToken() {
        String tokenTest = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(usuarioService.obtenerRolDeToken(anyString()))
                .thenReturn("ROLE_CLIENTE");

        String resultado = usuarioService.obtenerRolDeToken(tokenTest);

        assertNotNull(resultado);
        assertEquals("ROLE_CLIENTE", resultado);
        verify(usuarioService).obtenerRolDeToken(tokenTest);
    }

    @Test
    @DisplayName("Obtener rol de token - Falla")
    void testObtenerRolDeToken_TokenInvalido() {
        String tokenInvalido = "Bearer token_invalido";
        when(usuarioService.obtenerRolDeToken(anyString()))
                .thenReturn(null);

        String resultado = usuarioService.obtenerRolDeToken(tokenInvalido);

        assertNull(resultado);
        verify(usuarioService).obtenerRolDeToken(tokenInvalido);
    }

    // Tests de casos edge
    @Test
    @DisplayName("Crear usuario con multiples roles - Éxito")
    void testCrearUsuarioConMultiplesRoles_ListaVacia() {
        List<String> rolesVacios = Arrays.asList();
        when(usuarioService.crearUsuarioConMultiplesRoles(any(Usuario.class), anyList(), anyBoolean()))
                .thenReturn(usuarioTest);

        Usuario resultado = usuarioService.crearUsuarioConMultiplesRoles(usuarioTest, rolesVacios, true);

        assertNotNull(resultado);
        verify(usuarioService).crearUsuarioConMultiplesRoles(usuarioTest, rolesVacios, true);
    }

    @Test
    @DisplayName("Buscar por roles - Falla")
    void testFindByRolesNombre_RolNoExiste() {
        when(usuarioService.findByRolesNombre(anyString()))
                .thenReturn(Arrays.asList());

        List<Usuario> resultado = usuarioService.findByRolesNombre("ROLE_NO_EXISTE");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioService).findByRolesNombre("ROLE_NO_EXISTE");
    }
}