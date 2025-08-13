package com.arquitectura.ciudad;

import static org.assertj.core.api.Assertions.assertThat;

import com.arquitectura.ciudad.consumer.CiudadEventAdapterImpl;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.events.CiudadEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class CiudadEventAdapterTest {

    private CiudadEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new CiudadEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente CiudadEvent a Ciudad para creación")
    void testCreacionDesdeCiudadEvent() {
        // Arrange
        CiudadEvent event = new CiudadEvent();
        event.setId(1L);
        event.setNombre("Bogotá");

        Ciudad ciudad = new Ciudad();

        // Act
        Ciudad result = adapter.creacion(ciudad, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Bogotá");
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Ciudad existente")
    void testActualizacionCiudadExistente() {
        // Arrange
        CiudadEvent event = new CiudadEvent();
        event.setId(1L);
        event.setNombre("Medellín");

        Ciudad ciudadExistente = new Ciudad();
        ciudadExistente.setId(1L);
        ciudadExistente.setNombre("Cali");

        // Act
        Ciudad result = adapter.creacion(ciudadExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Medellín");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        CiudadEvent event = new CiudadEvent();
        event.setId(1L);
        event.setNombre(null);

        Ciudad ciudad = new Ciudad();

        // Act
        Ciudad result = adapter.creacion(ciudad, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        CiudadEvent event = new CiudadEvent();
        event.setId(1L);
        event.setNombre("Cartagena");

        Ciudad ciudad = new Ciudad();

        // Act
        Ciudad result = adapter.creacion(ciudad, event);

        // Assert
        assertThat(result).isSameAs(ciudad);
    }

    @Test
    @DisplayName("Debe manejar correctamente nombres con caracteres especiales")
    void testNombresConCaracteresEspeciales() {
        // Arrange
        CiudadEvent event = new CiudadEvent();
        event.setId(1L);
        event.setNombre("San Andrés & Providencia");

        Ciudad ciudad = new Ciudad();

        // Act
        Ciudad result = adapter.creacion(ciudad, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("San Andrés & Providencia");
    }
}
