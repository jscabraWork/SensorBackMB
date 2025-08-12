package com.arquitectura.dia;

import com.arquitectura.dia.consumer.DiaEventAdapterImpl;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.events.DiaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiaEventAdapterTest {
    private DiaEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new DiaEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente DiaEvent a Dia para creación")
    void testCreacionDesdeDiaEvent() {
        DiaEvent event = new DiaEvent();
        event.setId(1L);
        event.setNombre("Día 1");
        event.setEstado(1);

        Dia dia = new Dia();
        Dia result = adapter.creacion(dia, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Día 1");
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Dia existente")
    void testActualizacionDiaExistente() {
        DiaEvent event = new DiaEvent();
        event.setId(1L);
        event.setNombre("Día 2");
        event.setEstado(2);

        Dia diaExistente = new Dia();
        diaExistente.setId(1L);
        diaExistente.setNombre("Día Original");
        diaExistente.setEstado(1);

        Dia result = adapter.creacion(diaExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Día 2");
        assertThat(result.getEstado()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        DiaEvent event = new DiaEvent();
        event.setId(1L);
        event.setNombre(null);
        event.setEstado(null);

        Dia dia = new Dia();
        Dia result = adapter.creacion(dia, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.getEstado()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        DiaEvent event = new DiaEvent();
        event.setId(1L);
        event.setNombre("Día Test");
        event.setEstado(1);

        Dia dia = new Dia();
        Dia result = adapter.creacion(dia, event);
        assertThat(result).isSameAs(dia);
    }
}

