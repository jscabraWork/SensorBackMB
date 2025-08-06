package com.arquitectura.usuario;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.UsuariosApplication;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.usuario.controller.UsuarioController;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UsuarioController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = UsuariosApplication.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private Role roleAdmin;
    private Role roleOrganizador;

    @BeforeEach
    void setup() {
        roleAdmin = new Role();
        roleAdmin.setNombre("ROLE_ADMIN");

        roleOrganizador = new Role();
        roleOrganizador.setNombre("ROLE_ORGANIZADOR");

        usuario = Usuario.builder()
                .numeroDocumento("12345678")
                .nombre("Usuario Prueba")
                .correo("usuario@test.com")
                .roles(Arrays.asList(roleAdmin, roleOrganizador))
                .build();
    }

    // ------------------- CREAR USUARIOS -------------------

    @Test
    @DisplayName("Crear administrador - Éxito")
    void crearAdministradorExitoso() throws Exception {
        when(usuarioService.crearAdministrador(any(Usuario.class), anyBoolean()))
                .thenReturn(usuario);

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Revisa tu correo")));
    }

    @Test
    @DisplayName("Crear administrador - Datos duplicados")
    void crearAdministradorDuplicado() throws Exception {
        when(usuarioService.crearAdministrador(any(Usuario.class), anyBoolean()))
                .thenReturn(null);

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Los datos provisionados ya se encuentran registrados"));
    }

    // ------------------- OBTENER USUARIOS -------------------

    @Test
    @DisplayName("Obtener usuario por ID - Éxito")
    void getUsuarioPorIdExitoso() throws Exception {
        when(usuarioService.getCliente(anyString()))
                .thenReturn(usuario);

        mockMvc.perform(get("/admin/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.numeroDocumento").value("12345678"));
    }

    @Test
    @DisplayName("Obtener usuarios por rol - Éxito")
    void getUsuariosPorRol() throws Exception {
        List<Usuario> usuarios = Collections.singletonList(usuario);
        when(usuarioService.findByRolesNombre(anyString()))
                .thenReturn(usuarios);

        mockMvc.perform(get("/role/ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios", hasSize(1)));
    }

    // ------------------- ACTUALIZAR USUARIOS -------------------

    @Test
    @DisplayName("Actualizar usuario con roles - Éxito")
    void updateUsuarioConRolesExitoso() throws Exception {
        when(usuarioService.updateUsuarioConRoles(anyString(), any(Usuario.class), anyList()))
                .thenReturn(usuario);

        mockMvc.perform(put("/actualizar/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario actualizado exitosamente"));
    }

    @Test
    @DisplayName("Actualizar usuario - No encontrado")
    void updateUsuarioNoEncontrado() throws Exception {
        when(usuarioService.updateUsuarioConRoles(anyString(), any(Usuario.class), anyList()))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/actualizar/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value(containsString("Usuario no encontrado")));
    }

    // ------------------- MANEJO DE PAGINACIÓN -------------------

    @Test
    @DisplayName("Obtener usuarios paginados - Éxito")
    void getUsuariosPaginados() throws Exception {
        Page<Usuario> page = new PageImpl<>(Collections.singletonList(usuario));
        when(usuarioService.findClientesPaginados(anyLong(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/role/1/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios.content", hasSize(1)));
    }

    // ------------------- CAMBIO DE ESTADO -------------------

    @Test
    @DisplayName("Cambiar estado de usuario - Éxito")
    void cambiarEstadoUsuario() throws Exception {
        when(usuarioService.cambiarAccesoAlUsuario(anyString()))
                .thenReturn(true);

        mockMvc.perform(put("/enabled/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // ------------------- PRUEBAS DE VALIDACIÓN -------------------

    @Test
    @DisplayName("Crear usuario con múltiples roles - Éxito")
    void crearUsuarioConRolesExitoso() throws Exception {
        // Prepara los roles y usuario de prueba
        Role roleAdmin = new Role();
        roleAdmin.setNombre("ROLE_ADMIN");
        Role roleOrg = new Role();
        roleOrg.setNombre("ROLE_ORGANIZADOR");

        Usuario usuario = Usuario.builder()
                .numeroDocumento("12345678")
                .nombre("Usuario Prueba")
                .correo("usuario@test.com")
                .roles(Arrays.asList(roleAdmin, roleOrg))
                .build();

        // Simula el comportamiento del service
        Mockito.when(usuarioService.crearUsuarioConMultiplesRoles(
                Mockito.any(Usuario.class),
                Mockito.anyList(),
                Mockito.eq(true))
        ).thenReturn(usuario);

        mockMvc.perform(post("/crear/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario creado exitosamente con los roles: ROLE_ADMIN, ROLE_ORGANIZADOR"));
    }

    @Test
    @DisplayName("Crear usuario con múltiples roles - Usuario ya registrado")
    void crearUsuarioConRolesDuplicado() throws Exception {
        // Prepara los roles y usuario de prueba
        Role roleAdmin = new Role();
        roleAdmin.setNombre("ROLE_ADMIN");

        Usuario usuario = Usuario.builder()
                .numeroDocumento("12345678")
                .nombre("Usuario Prueba")
                .correo("usuario@test.com")
                .roles(Collections.singletonList(roleAdmin))
                .build();

        // Simula el comportamiento del service: usuario ya existe
        Mockito.when(usuarioService.crearUsuarioConMultiplesRoles(
                Mockito.any(Usuario.class),
                Mockito.anyList(),
                Mockito.eq(true))
        ).thenReturn(null);

        mockMvc.perform(post("/crear/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Los datos ingresados ya se encuentran registrados"));
    }


    @Test
    @DisplayName("Actualizar usuario con múltiples roles - Error interno")
    void updateUsuarioConRolesError() throws Exception {
        // Prepara el usuario de prueba
        Role roleAdmin = new Role();
        roleAdmin.setNombre("ROLE_ADMIN");
        Usuario usuario = Usuario.builder()
                .numeroDocumento("12345678")
                .nombre("Usuario Actualizado")
                .correo("usuario@test.com")
                .roles(Collections.singletonList(roleAdmin))
                .build();

        // Simula un error en el service
        Mockito.when(usuarioService.updateUsuarioConRoles(
                Mockito.anyString(),
                Mockito.any(Usuario.class),
                Mockito.anyList())
        ).thenThrow(new RuntimeException("Error de actualización"));

        mockMvc.perform(put("/actualizar/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error al actualizar usuario: Error de actualización"));
    }


}