package com.arquitectura.ingreso;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.ingreso.consumer.IngresoEventAdapterImpl;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class IngresoEventAdapterTest {

    @InjectMocks
    private IngresoEventAdapterImpl adapter;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private DiaRepository diaRepository;

    private Ticket ticket;
    private Dia dia;
    private LocalDateTime fechaIngreso;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fechaIngreso = LocalDateTime.of(2024, 1, 15, 10, 30);

        // Crear entidades Ticket y Dia para los tests
        ticket = new Ticket();
        ticket.setId(100L);

        dia = new Dia();
        dia.setId(10L);
        dia.setNombre("Día de prueba");

        // Configurar comportamiento de los mocks
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(diaRepository.findById(10L)).thenReturn(Optional.of(dia));
    }

    @Test
    @DisplayName("Debe convertir correctamente IngresoEvent a Ingreso para creación")
    void testCreacionDesdeIngresoEvent() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(true);
        event.setTicketId(100L);
        event.setDiaId(10L);
        event.setFechaIngreso(fechaIngreso);

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isTrue();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaIngreso);
        assertThat(result.getTicket()).isNotNull();
        assertThat(result.getTicket().getId()).isEqualTo(100L);
        assertThat(result.getDia()).isNotNull();
        assertThat(result.getDia().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Ingreso existente")
    void testActualizacionIngresoExistente() {
        // Arrange
        LocalDateTime nuevaFecha = LocalDateTime.of(2024, 2, 20, 14, 45);
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(false);
        event.setTicketId(100L);
        event.setDiaId(10L);
        event.setFechaIngreso(nuevaFecha);

        Ingreso ingresoExistente = new Ingreso();
        ingresoExistente.setId(1L);
        ingresoExistente.setUtilizado(true);
        ingresoExistente.setFechaIngreso(fechaIngreso);

        // Act
        Ingreso result = adapter.creacion(ingresoExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getFechaIngreso()).isEqualTo(nuevaFecha);
        assertThat(result.getTicket()).isNotNull();
        assertThat(result.getTicket().getId()).isEqualTo(100L);
        assertThat(result.getDia()).isNotNull();
        assertThat(result.getDia().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(false);
        event.setTicketId(null);
        event.setDiaId(null);
        event.setFechaIngreso(null);

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getFechaIngreso()).isNull();
        assertThat(result.getTicket()).isNull();
        assertThat(result.getDia()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(true);
        event.setTicketId(100L);
        event.setDiaId(10L);
        event.setFechaIngreso(fechaIngreso);

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isSameAs(ingreso);
    }

    @Test
    @DisplayName("Debe manejar ticket no encontrado")
    void testTicketNoEncontrado() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(true);
        event.setTicketId(999L);
        event.setDiaId(10L);
        event.setFechaIngreso(fechaIngreso);

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isTrue();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaIngreso);
        assertThat(result.getTicket()).isNull();
        assertThat(result.getDia()).isNotNull();
        assertThat(result.getDia().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe manejar día no encontrado")
    void testDiaNoEncontrado() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(true);
        event.setTicketId(100L);
        event.setDiaId(999L);
        event.setFechaIngreso(fechaIngreso);

        when(diaRepository.findById(999L)).thenReturn(Optional.empty());

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isTrue();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaIngreso);
        assertThat(result.getTicket()).isNotNull();
        assertThat(result.getTicket().getId()).isEqualTo(100L);
        assertThat(result.getDia()).isNull();
    }

    @Test
    @DisplayName("Debe manejar tanto ticket como día no encontrados")
    void testTicketYDiaNoEncontrados() {
        // Arrange
        IngresoEvent event = new IngresoEvent();
        event.setId(1L);
        event.setUtilizado(false);
        event.setTicketId(999L);
        event.setDiaId(888L);
        event.setFechaIngreso(fechaIngreso);

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        when(diaRepository.findById(888L)).thenReturn(Optional.empty());

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaIngreso);
        assertThat(result.getTicket()).isNull();
        assertThat(result.getDia()).isNull();
    }

    @Test
    @DisplayName("Debe manejar correctamente el flag utilizado")
    void testManejoFlagUtilizado() {
        // Arrange - Caso false
        IngresoEvent eventFalse = new IngresoEvent();
        eventFalse.setId(1L);
        eventFalse.setUtilizado(false);
        eventFalse.setTicketId(100L);
        eventFalse.setDiaId(10L);
        eventFalse.setFechaIngreso(fechaIngreso);

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, eventFalse);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isUtilizado()).isFalse();
    }

    @Test
    @DisplayName("Debe manejar correctamente fechas pasadas y futuras")
    void testManejoFechas() {
        // Arrange - Fecha pasada
        LocalDateTime fechaPasada = LocalDateTime.of(2020, 1, 1, 0, 0);
        IngresoEvent eventPasado = new IngresoEvent();
        eventPasado.setId(1L);
        eventPasado.setUtilizado(true);
        eventPasado.setTicketId(100L);
        eventPasado.setDiaId(10L);
        eventPasado.setFechaIngreso(fechaPasada);

        Ingreso ingreso = new Ingreso();

        // Act
        Ingreso result = adapter.creacion(ingreso, eventPasado);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaPasada);

        // Arrange - Fecha futura
        LocalDateTime fechaFutura = LocalDateTime.of(2030, 12, 31, 23, 59);
        eventPasado.setFechaIngreso(fechaFutura);

        // Act
        result = adapter.creacion(ingreso, eventPasado);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFechaIngreso()).isEqualTo(fechaFutura);
    }
}
