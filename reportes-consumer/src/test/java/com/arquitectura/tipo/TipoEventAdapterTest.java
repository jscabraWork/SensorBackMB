package com.arquitectura.tipo;

import static org.assertj.core.api.Assertions.assertThat;

import com.arquitectura.tipo.consumer.TipoEventAdapterImpl;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.events.TipoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TipoEventAdapterTest {
    private TipoEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new TipoEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente TipoEvent a Tipo para creación")
    void testCreacionDesdeTipoEvent() {
        TipoEvent event = new TipoEvent();
        event.setId(1L);
        event.setNombre("Tipo General");

        Tipo tipo = new Tipo();
        Tipo result = adapter.creacion(tipo, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Tipo General");
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Tipo existente")
    void testActualizacionTipoExistente() {
        TipoEvent event = new TipoEvent();
        event.setId(1L);
        event.setNombre("Tipo VIP");

        Tipo tipoExistente = new Tipo();
        tipoExistente.setId(1L);
        tipoExistente.setNombre("Tipo General");

        Tipo result = adapter.creacion(tipoExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Tipo VIP");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        TipoEvent event = new TipoEvent();
        event.setId(1L);
        event.setNombre(null);

        Tipo tipo = new Tipo();
        Tipo result = adapter.creacion(tipo, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        TipoEvent event = new TipoEvent();
        event.setId(1L);
        event.setNombre("Tipo Test");

        Tipo tipo = new Tipo();
        Tipo result = adapter.creacion(tipo, event);
        assertThat(result).isSameAs(tipo);
    }

    @Test
    @DisplayName("Debe manejar correctamente evento con ID nulo")
    void testManejoIdNulo() {
        TipoEvent event = new TipoEvent();
        event.setId(null);
        event.setNombre("Tipo Sin ID");

        Tipo tipo = new Tipo();
        Tipo result = adapter.creacion(tipo, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getNombre()).isEqualTo("Tipo Sin ID");
    }

    @Test
    @DisplayName("Debe sobrescribir datos existentes con datos del evento")
    void testSobrescribirDatosExistentes() {
        TipoEvent event = new TipoEvent();
        event.setId(2L);
        event.setNombre("Tipo Nuevo");

        Tipo tipoConDatos = new Tipo();
        tipoConDatos.setId(1L);
        tipoConDatos.setNombre("Tipo Anterior");

        Tipo result = adapter.creacion(tipoConDatos, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L); // Debe tomar el ID del evento
        assertThat(result.getNombre()).isEqualTo("Tipo Nuevo"); // Debe tomar el nombre del evento
        assertThat(result).isSameAs(tipoConDatos); // Debe ser el mismo objeto
    }
}
