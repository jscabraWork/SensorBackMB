package com.arquitectura.temporada;

import static org.assertj.core.api.Assertions.assertThat;

import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.temporada.consumer.TemporadaEventAdapterImpl;
import com.arquitectura.temporada.entity.Temporada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

public class TemporadaEventAdapterTest {

    @InjectMocks
    private TemporadaEventAdapterImpl adapter;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fechaInicio = LocalDateTime.of(2024, 6, 1, 0, 0);
        fechaFin = LocalDateTime.of(2024, 8, 31, 23, 59);
    }

    @Test
    @DisplayName("Debe convertir correctamente TemporadaEvent a Temporada para creación")
    void testCreacionDesdeTemporadaEvent() {
        // Arrange
        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Verano 2024");
        event.setFechaInicio(fechaInicio);
        event.setFechaFin(fechaFin);
        event.setEstado(1); // ACTIVA

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Temporada Verano 2024");
        assertThat(result.getFechaInicio()).isEqualTo(fechaInicio);
        assertThat(result.getFechaFin()).isEqualTo(fechaFin);
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Temporada existente")
    void testActualizacionTemporadaExistente() {
        // Arrange
        LocalDateTime nuevaFechaInicio = LocalDateTime.of(2024, 6, 15, 0, 0);
        LocalDateTime nuevaFechaFin = LocalDateTime.of(2024, 9, 15, 23, 59);

        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Verano 2024 - Actualizada");
        event.setFechaInicio(nuevaFechaInicio);
        event.setFechaFin(nuevaFechaFin);
        event.setEstado(2); // INACTIVA

        Temporada temporadaExistente = new Temporada();
        temporadaExistente.setId(1L);
        temporadaExistente.setNombre("Temporada Verano 2024");
        temporadaExistente.setFechaInicio(fechaInicio);
        temporadaExistente.setFechaFin(fechaFin);
        temporadaExistente.setEstado(1);

        // Act
        Temporada result = adapter.creacion(temporadaExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Temporada Verano 2024 - Actualizada");
        assertThat(result.getFechaInicio()).isEqualTo(nuevaFechaInicio);
        assertThat(result.getFechaFin()).isEqualTo(nuevaFechaFin);
        assertThat(result.getEstado()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre(null);
        event.setFechaInicio(null);
        event.setFechaFin(null);
        event.setEstado(null);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.getFechaInicio()).isNull();
        assertThat(result.getFechaFin()).isNull();
        assertThat(result.getEstado()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Verano 2024");
        event.setFechaInicio(fechaInicio);
        event.setFechaFin(fechaFin);
        event.setEstado(1);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isSameAs(temporada);
    }

    @Test
    @DisplayName("Debe manejar correctamente diferentes estados")
    void testDiferentesEstados() {
        // Arrange
        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Test");
        event.setFechaInicio(fechaInicio);
        event.setFechaFin(fechaFin);

        Temporada temporada = new Temporada();

        // Test ACTIVA
        event.setEstado(1);
        Temporada result1 = adapter.creacion(temporada, event);
        assertThat(result1.getEstado()).isEqualTo(1);

        // Test INACTIVA
        event.setEstado(2);
        Temporada result2 = adapter.creacion(temporada, event);
        assertThat(result2.getEstado()).isEqualTo(2);

        // Test PAUSADA
        event.setEstado(3);
        Temporada result3 = adapter.creacion(temporada, event);
        assertThat(result3.getEstado()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe manejar correctamente fechas en diferentes años")
    void testFechasDiferentesAnios() {
        // Arrange
        LocalDateTime fechaInicio2025 = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fechaFin2025 = LocalDateTime.of(2025, 12, 31, 23, 59);

        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada 2025");
        event.setFechaInicio(fechaInicio2025);
        event.setFechaFin(fechaFin2025);
        event.setEstado(1);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFechaInicio()).isEqualTo(fechaInicio2025);
        assertThat(result.getFechaFin()).isEqualTo(fechaFin2025);
        assertThat(result.getNombre()).isEqualTo("Temporada 2025");
    }

    @Test
    @DisplayName("Debe manejar correctamente nombres largos")
    void testNombresLargos() {
        // Arrange
        String nombreLargo = "Temporada Especial de Verano 2024 con Eventos Deportivos y Culturales";

        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre(nombreLargo);
        event.setFechaInicio(fechaInicio);
        event.setFechaFin(fechaFin);
        event.setEstado(1);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo(nombreLargo);
    }

    @Test
    @DisplayName("Debe manejar correctamente fechas con precisión de minutos")
    void testFechasConPrecisionMinutos() {
        // Arrange
        LocalDateTime fechaInicioDetallada = LocalDateTime.of(2024, 6, 1, 10, 30, 45);
        LocalDateTime fechaFinDetallada = LocalDateTime.of(2024, 8, 31, 20, 15, 30);

        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Detallada");
        event.setFechaInicio(fechaInicioDetallada);
        event.setFechaFin(fechaFinDetallada);
        event.setEstado(1);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFechaInicio()).isEqualTo(fechaInicioDetallada);
        assertThat(result.getFechaFin()).isEqualTo(fechaFinDetallada);
    }

    @Test
    @DisplayName("Debe manejar correctamente temporadas de un solo día")
    void testTemporadaUnSoloDia() {
        // Arrange
        LocalDateTime fechaUnDia = LocalDateTime.of(2024, 7, 15, 0, 0);
        LocalDateTime fechaFinUnDia = LocalDateTime.of(2024, 7, 15, 23, 59);

        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Un Día");
        event.setFechaInicio(fechaUnDia);
        event.setFechaFin(fechaFinUnDia);
        event.setEstado(1);

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFechaInicio()).isEqualTo(fechaUnDia);
        assertThat(result.getFechaFin()).isEqualTo(fechaFinUnDia);
        assertThat(result.getNombre()).isEqualTo("Temporada Un Día");
    }

    @Test
    @DisplayName("Debe manejar estado cero correctamente")
    void testEstadoCero() {
        // Arrange
        TemporadaEvent event = new TemporadaEvent();
        event.setId(1L);
        event.setNombre("Temporada Estado Cero");
        event.setFechaInicio(fechaInicio);
        event.setFechaFin(fechaFin);
        event.setEstado(0); // Estado cero

        Temporada temporada = new Temporada();

        // Act
        Temporada result = adapter.creacion(temporada, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo(0);
    }
}
