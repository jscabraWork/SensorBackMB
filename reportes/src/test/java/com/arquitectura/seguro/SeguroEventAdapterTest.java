package com.arquitectura.seguro;

import com.arquitectura.events.SeguroEvent;
import com.arquitectura.seguro.consumer.SeguroEventAdapterImpl;
import com.arquitectura.seguro.entity.Seguro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class SeguroEventAdapterTest {

    @InjectMocks
    private SeguroEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe convertir correctamente SeguroEvent a Seguro para creación")
    void testCreacionDesdeSeguroEvent() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(25000.0);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Act
        Seguro result = adapter.creacion(seguro, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isEqualTo(25000.0);
        assertThat(result.isReclamado()).isFalse();
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Seguro existente")
    void testActualizacionSeguroExistente() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(50000.0);
        event.setReclamado(true);

        Seguro seguroExistente = new Seguro();
        seguroExistente.setId(1L);
        seguroExistente.setValor(25000.0);
        seguroExistente.setReclamado(false);

        // Act
        Seguro result = adapter.creacion(seguroExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isEqualTo(50000.0);
        assertThat(result.isReclamado()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(null);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Act
        Seguro result = adapter.creacion(seguro, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isNull();
        assertThat(result.isReclamado()).isFalse();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(25000.0);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Act
        Seguro result = adapter.creacion(seguro, event);

        // Assert
        assertThat(result).isSameAs(seguro);
    }

    @Test
    @DisplayName("Debe manejar diferentes estados de reclamación")
    void testDiferentesEstadosReclamacion() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(25000.0);

        Seguro seguro = new Seguro();

        // Test no reclamado
        event.setReclamado(false);
        Seguro result1 = adapter.creacion(seguro, event);
        assertThat(result1.isReclamado()).isFalse();

        // Test reclamado
        event.setReclamado(true);
        Seguro result2 = adapter.creacion(seguro, event);
        assertThat(result2.isReclamado()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar diferentes valores monetarios")
    void testDiferentesValoresMonetarios() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Test valor pequeño
        event.setValor(5000.0);
        Seguro result1 = adapter.creacion(seguro, event);
        assertThat(result1.getValor()).isEqualTo(5000.0);

        // Test valor mediano
        event.setValor(25000.0);
        Seguro result2 = adapter.creacion(seguro, event);
        assertThat(result2.getValor()).isEqualTo(25000.0);

        // Test valor grande
        event.setValor(100000.0);
        Seguro result3 = adapter.creacion(seguro, event);
        assertThat(result3.getValor()).isEqualTo(100000.0);
    }

    @Test
    @DisplayName("Debe manejar valores decimales")
    void testValoresDecimales() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(25500.75);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Act
        Seguro result = adapter.creacion(seguro, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getValor()).isEqualTo(25500.75);
    }

    @Test
    @DisplayName("Debe manejar cambio de estado de reclamación")
    void testCambioEstadoReclamacion() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(25000.0);
        event.setReclamado(true);

        Seguro seguroExistente = new Seguro();
        seguroExistente.setId(1L);
        seguroExistente.setValor(25000.0);
        seguroExistente.setReclamado(false); // Estaba sin reclamar

        // Act
        Seguro result = adapter.creacion(seguroExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isEqualTo(25000.0);
        assertThat(result.isReclamado()).isTrue(); // Ahora está reclamado
    }

    @Test
    @DisplayName("Debe manejar valor cero")
    void testValorCero() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(0.0);
        event.setReclamado(false);

        Seguro seguro = new Seguro();

        // Act
        Seguro result = adapter.creacion(seguro, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getValor()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Debe manejar actualización de valor manteniendo estado")
    void testActualizacionValorManteniendoEstado() {
        // Arrange
        SeguroEvent event = new SeguroEvent();
        event.setId(1L);
        event.setValor(50000.0);
        event.setReclamado(true);

        Seguro seguroExistente = new Seguro();
        seguroExistente.setId(1L);
        seguroExistente.setValor(25000.0);
        seguroExistente.setReclamado(true); // Ya estaba reclamado

        // Act
        Seguro result = adapter.creacion(seguroExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isEqualTo(50000.0); // Valor actualizado
        assertThat(result.isReclamado()).isTrue(); // Estado mantenido
    }
}
