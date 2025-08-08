package com.arquitectura.imagen;

import com.arquitectura.JwtAuthenticationFilter;
import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.TestSecurityConfig;
import com.arquitectura.imagen.controller.ImagenController;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.service.ImagenService;
import com.arquitectura.evento.entity.Evento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ImagenController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@MockBean(JwtAuthenticationFilter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class ImagenControllerTest {

    @MockBean
    private ImagenService imagenService;

    @Autowired
    private ImagenController imagenController;

    private Imagen imagen;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Evento Test");

        imagen = new Imagen("test.jpg", 1, evento);
        imagen.setId(1L);

        mockFile = new MockMultipartFile(
                "files",
                "test.jpg",
                "image/jpeg",
                "contenido test".getBytes()
        );
    }

    @Test
    void testCrearImagenExitoso() throws Exception {
        List<MultipartFile> files = Collections.singletonList(mockFile);
        List<Integer> tipos = Collections.singletonList(1);
        when(imagenService.crear(any(MultipartFile.class), eq(1L), eq(1)))
                .thenReturn(imagen);

        ResponseEntity<?> response = imagenController.crear(files, 1L, tipos);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Se subieron los archivos correctamente", responseBody.get("message"));

        verify(imagenService, times(1)).crear(any(MultipartFile.class), eq(1L), eq(1));
    }

    @Test
    void testCrearImagenConError() throws Exception {
        List<MultipartFile> files = Collections.singletonList(mockFile);
        List<Integer> tipos = Collections.singletonList(1);
        when(imagenService.crear(any(MultipartFile.class), eq(1L), eq(1)))
                .thenThrow(new RuntimeException("Error al subir archivo"));

        ResponseEntity<?> response = imagenController.crear(files, 1L, tipos);

        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
        assertEquals("Error al subir archivo", response.getBody());

        verify(imagenService, times(1)).crear(any(MultipartFile.class), eq(1L), eq(1));
    }

    @Test
    void testCrearImagenSinArchivos() throws Exception {
        List<MultipartFile> files = Collections.emptyList();
        List<Integer> tipos = Collections.emptyList();

        ResponseEntity<?> response = imagenController.crear(files, 1L, tipos);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Se subieron los archivos correctamente", responseBody.get("message"));

        verify(imagenService, never()).crear(any(MultipartFile.class), any(Long.class), any(Integer.class));
    }

    @Test
    void testCrearMultiplesImagenes() throws Exception {
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.jpg",
                "image/jpeg",
                "contenido test 2".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(mockFile, file2);
        List<Integer> tipos = Arrays.asList(1, 1);

        Imagen imagen2 = new Imagen("test2.jpg", 1, imagen.getEvento());
        imagen2.setId(2L);

        when(imagenService.crear(any(MultipartFile.class), eq(1L), eq(1)))
                .thenReturn(imagen, imagen2);

        ResponseEntity<?> response = imagenController.crear(files, 1L, tipos);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(imagenService, times(2)).crear(any(MultipartFile.class), eq(1L), eq(1));
    }

    @Test
    void testCrearImagenConArchivoVacio() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "files",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        List<MultipartFile> files = Collections.singletonList(emptyFile);
        List<Integer> tipos = Collections.singletonList(1);

        ResponseEntity<?> response = imagenController.crear(files, 1L, tipos);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}