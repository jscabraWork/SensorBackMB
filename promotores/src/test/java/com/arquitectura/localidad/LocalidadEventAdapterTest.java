package com.arquitectura.localidad;

import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.localidad.consumer.LocalidadEventAdapterImpl;
import com.arquitectura.localidad.entity.Localidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalidadEventAdapterTest {

    private LocalidadEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalidadEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente LocalidadEvent a Localidad para creación")
    void testCreacionDesdeLocalidadEvent() {
        LocalidadEvent event = new LocalidadEvent();
        event.setId(1L);
        event.setNombre("VIP");
        event.setTipo(1);
        event.setDescripcion("Zona VIP");
        event.setAporteMinimo(10000.00);

        Localidad localidad = new Localidad();
        Localidad result = adapter.creacion(localidad, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("VIP");
        assertThat(result.getTipo()).isEqualTo(1);
        assertThat(result.getAporteMinimo()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Localidad existente")
    void testActualizacionLocalidadExistente() {
        LocalidadEvent event = new LocalidadEvent();
        event.setId(1L);
        event.setNombre("PLATEA");
        event.setTipo(2);
        event.setDescripcion("Zona Platea");
        event.setAporteMinimo(20000.00);

        Localidad localidadExistente = new Localidad();
        localidadExistente.setId(1L);
        localidadExistente.setNombre("VIP");
        localidadExistente.setTipo(1);
        localidadExistente.setAporteMinimo(10000.00);

        Localidad result = adapter.creacion(localidadExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("PLATEA");
        assertThat(result.getTipo()).isEqualTo(2);
        assertThat(result.getAporteMinimo()).isEqualTo(20000.00);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        LocalidadEvent event = new LocalidadEvent();
        event.setId(1L);
        event.setNombre(null);
        event.setTipo(null);
        event.setDescripcion(null);
        event.setAporteMinimo(null);

        Localidad localidad = new Localidad();
        Localidad result = adapter.creacion(localidad, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.getTipo()).isNull();
        assertThat(result.getAporteMinimo()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        LocalidadEvent event = new LocalidadEvent();
        event.setId(1L);
        event.setNombre("VIP");
        event.setTipo(1);

        Localidad localidad = new Localidad();
        Localidad result = adapter.creacion(localidad, event);
        assertThat(result).isSameAs(localidad);
    }
}
