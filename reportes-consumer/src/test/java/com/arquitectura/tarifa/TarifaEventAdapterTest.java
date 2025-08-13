package com.arquitectura.tarifa;

import static org.assertj.core.api.Assertions.assertThat;

import com.arquitectura.tarifa.consumer.TarifaEventAdapterImpl;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.events.TarifaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TarifaEventAdapterTest {
    private TarifaEventAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        adapter = new TarifaEventAdapterImpl();
    }

    @Test
    @DisplayName("Debe convertir correctamente TarifaEvent a Tarifa para creación")
    void testCreacionDesdeTarifaEvent() {
        TarifaEvent event = new TarifaEvent();
        event.setId(1L);
        event.setNombre("Tarifa General");
        event.setPrecio(50000.0);
        event.setServicio(5000.0);
        event.setIva(9500.0);
        event.setEstado(1);

        Tarifa tarifa = new Tarifa();
        Tarifa result = adapter.creacion(tarifa, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Tarifa General");
        assertThat(result.getPrecio()).isEqualTo(50000.0);
        assertThat(result.getServicio()).isEqualTo(5000.0);
        assertThat(result.getIva()).isEqualTo(9500.0);
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Tarifa existente")
    void testActualizacionTarifaExistente() {
        TarifaEvent event = new TarifaEvent();
        event.setId(1L);
        event.setNombre("Tarifa VIP");
        event.setPrecio(100000.0);
        event.setServicio(10000.0);
        event.setIva(19000.0);
        event.setEstado(1);

        Tarifa tarifaExistente = new Tarifa();
        tarifaExistente.setId(1L);
        tarifaExistente.setNombre("Tarifa General");
        tarifaExistente.setPrecio(50000.0);
        tarifaExistente.setServicio(5000.0);
        tarifaExistente.setIva(9500.0);
        tarifaExistente.setEstado(0);

        Tarifa result = adapter.creacion(tarifaExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Tarifa VIP");
        assertThat(result.getPrecio()).isEqualTo(100000.0);
        assertThat(result.getServicio()).isEqualTo(10000.0);
        assertThat(result.getIva()).isEqualTo(19000.0);
        assertThat(result.getEstado()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        TarifaEvent event = new TarifaEvent();
        event.setId(1L);
        event.setNombre(null);
        event.setPrecio(null);
        event.setServicio(null);
        event.setIva(null);
        event.setEstado(null);

        Tarifa tarifa = new Tarifa();
        Tarifa result = adapter.creacion(tarifa, event);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.getPrecio()).isNull();
        assertThat(result.getServicio()).isNull();
        assertThat(result.getIva()).isNull();
        assertThat(result.getEstado()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        TarifaEvent event = new TarifaEvent();
        event.setId(1L);
        event.setNombre("Tarifa Test");
        event.setEstado(1);

        Tarifa tarifa = new Tarifa();
        Tarifa result = adapter.creacion(tarifa, event);
        assertThat(result).isSameAs(tarifa);
    }
}
