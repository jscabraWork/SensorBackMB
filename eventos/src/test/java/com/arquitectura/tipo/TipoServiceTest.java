package com.arquitectura.tipo;

import com.arquitectura.MicroservicioEventosApplication;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.entity.TipoRepository;
import com.arquitectura.tipo.service.TipoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = MicroservicioEventosApplication.class)
public class TipoServiceTest {

    @MockBean
    private TipoRepository tipoRepository;

    @Autowired
    private TipoService tipoService;

    private Tipo tipo1;
    private Tipo tipo2;

    @BeforeEach
    void setUp() {
        tipo1 = Tipo.builder()
                .id(1L)
                .nombre("Tipo 1")
                .build();

        tipo2 = Tipo.builder()
                .id(2L)
                .nombre("Tipo 2")
                .build();
    }

    @Test
    @DisplayName("Buscar todos los tipos")
    void testFindAll() {
        when(tipoRepository.findAll()).thenReturn(Arrays.asList(tipo1, tipo2));

        List<Tipo> result = tipoService.findAll();

        assertEquals(2, result.size());
        verify(tipoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar tipo por ID existente")
    void testFindById() {
        when(tipoRepository.findById(1L)).thenReturn(Optional.of(tipo1));

        Tipo result = tipoService.findById(1L);

        assertTrue(result!=null);
        assertEquals("Tipo 1", result.getNombre());
        verify(tipoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Buscar tipo por ID no existente")
    void testFindByIdNotFound() {
        when(tipoRepository.findById(99L)).thenReturn(Optional.empty());

        Tipo result = tipoService.findById(99L);

        assertFalse(result!=null);
        verify(tipoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Guardar un nuevo tipo")
    void testSave() {
        when(tipoRepository.save(any(Tipo.class))).thenReturn(tipo1);

        Tipo result = tipoService.save(tipo1);

        assertEquals("Tipo 1", result.getNombre());
        verify(tipoRepository, times(1)).save(tipo1);
    }

    @Test
    @DisplayName("Eliminar tipo por ID")
    void testDeleteById() {
        doNothing().when(tipoRepository).deleteById(1L);

        assertDoesNotThrow(() -> tipoService.deleteById(1L));

        verify(tipoRepository, times(1)).deleteById(1L);
    }


}