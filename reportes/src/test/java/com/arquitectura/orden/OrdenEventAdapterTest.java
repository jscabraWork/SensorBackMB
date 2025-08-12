package com.arquitectura.orden;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.orden.consumer.OrdenEventAdapterImpl;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class OrdenEventAdapterTest {

    @InjectMocks
    private OrdenEventAdapterImpl adapter;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private TarifaRepository tarifaRepository;

    @Mock
    private TicketRepository ticketRepository;

    private Cliente cliente;
    private Evento evento;
    private Tarifa tarifa;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear entidades para los tests
        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Juan Pérez");

        evento = new Evento();
        evento.setId(100L);
        evento.setNombre("Evento de prueba");

        tarifa = new Tarifa();
        tarifa.setId(10L);
        tarifa.setNombre("Tarifa VIP");

        ticket1 = new Ticket();
        ticket1.setId(1L);

        ticket2 = new Ticket();
        ticket2.setId(2L);

        // Configurar comportamiento de los mocks
        when(clienteRepository.findById("12345678")).thenReturn(Optional.of(cliente));
        when(eventoRepository.findById(100L)).thenReturn(Optional.of(evento));
        when(tarifaRepository.findById(10L)).thenReturn(Optional.of(tarifa));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
    }

    @Test
    @DisplayName("Debe convertir correctamente OrdenEvent a Orden para creación")
    void testCreacionDesdeOrdenEvent() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1); // ACEPTADA
        event.setTipo(1); // COMPRA ESTANDAR
        event.setEventoId(100L);
        event.setValorOrden(150000.0);
        event.setValorSeguro(5000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setTarifaId(10L);

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(1);
        assertThat(result.getValorOrden()).isEqualTo(150000.0);
        assertThat(result.getValorSeguro()).isEqualTo(5000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getCliente().getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getEvento().getId()).isEqualTo(100L);
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTarifa().getId()).isEqualTo(10L);
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Orden existente")
    void testActualizacionOrdenExistente() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(2); // RECHAZADA
        event.setTipo(2); // ADICIONES
        event.setEventoId(100L);
        event.setValorOrden(250000.0);
        event.setValorSeguro(8000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setTarifaId(10L);

        Orden ordenExistente = new Orden();
        ordenExistente.setId(1L);
        ordenExistente.setEstado(1);
        ordenExistente.setTipo(1);
        ordenExistente.setValorOrden(150000.0);
        ordenExistente.setValorSeguro(5000.0);

        // Act
        Orden result = adapter.creacion(ordenExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(2);
        assertThat(result.getTipo()).isEqualTo(2);
        assertThat(result.getValorOrden()).isEqualTo(250000.0);
        assertThat(result.getValorSeguro()).isEqualTo(8000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(null);
        event.setTipo(null);
        event.setEventoId(null);
        event.setValorOrden(null);
        event.setValorSeguro(null);
        event.setTicketsIds(null);
        event.setClienteId(null);
        event.setTarifaId(null);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());
        when(eventoRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(tarifaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isNull();
        assertThat(result.getTipo()).isNull();
        assertThat(result.getValorOrden()).isNull();
        assertThat(result.getValorSeguro()).isNull();
        assertThat(result.getCliente()).isNull();
        assertThat(result.getEvento()).isNull();
        assertThat(result.getTarifa()).isNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).isEmpty();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setClienteId("12345678");

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isSameAs(orden);
    }

    @Test
    @DisplayName("Debe manejar diferentes estados de orden")
    void testDiferentesEstadosOrden() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setValorOrden(150000.0);
        event.setClienteId("12345678");

        Orden orden = new Orden();

        // Test ACEPTADA
        event.setEstado(1);
        Orden result1 = adapter.creacion(orden, event);
        assertThat(result1.getEstado()).isEqualTo(1);

        // Test RECHAZADA
        event.setEstado(2);
        Orden result2 = adapter.creacion(orden, event);
        assertThat(result2.getEstado()).isEqualTo(2);

        // Test EN PROCESO
        event.setEstado(3);
        Orden result3 = adapter.creacion(orden, event);
        assertThat(result3.getEstado()).isEqualTo(3);

        // Test DEVOLUCION
        event.setEstado(4);
        Orden result4 = adapter.creacion(orden, event);
        assertThat(result4.getEstado()).isEqualTo(4);

        // Test FRAUDE
        event.setEstado(5);
        Orden result5 = adapter.creacion(orden, event);
        assertThat(result5.getEstado()).isEqualTo(5);

        // Test UPGRADE
        event.setEstado(6);
        Orden result6 = adapter.creacion(orden, event);
        assertThat(result6.getEstado()).isEqualTo(6);
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de orden")
    void testDiferentesTiposOrden() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setValorOrden(150000.0);
        event.setClienteId("12345678");

        Orden orden = new Orden();

        // Test COMPRA ESTANDAR
        event.setTipo(1);
        Orden result1 = adapter.creacion(orden, event);
        assertThat(result1.getTipo()).isEqualTo(1);

        // Test ADICIONES
        event.setTipo(2);
        Orden result2 = adapter.creacion(orden, event);
        assertThat(result2.getTipo()).isEqualTo(2);

        // Test CREAR ALCANCIA
        event.setTipo(3);
        Orden result3 = adapter.creacion(orden, event);
        assertThat(result3.getTipo()).isEqualTo(3);

        // Test APORTAR A ALCANCIA
        event.setTipo(4);
        Orden result4 = adapter.creacion(orden, event);
        assertThat(result4.getTipo()).isEqualTo(4);

        // Test TRASPASO DE TICKET
        event.setTipo(5);
        Orden result5 = adapter.creacion(orden, event);
        assertThat(result5.getTipo()).isEqualTo(5);

        // Test ASIGNACION
        event.setTipo(6);
        Orden result6 = adapter.creacion(orden, event);
        assertThat(result6.getTipo()).isEqualTo(6);
    }

    @Test
    @DisplayName("Debe manejar cliente no encontrado")
    void testClienteNoEncontrado() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setValorOrden(150000.0);
        event.setClienteId("99999999");

        when(clienteRepository.findById("99999999")).thenReturn(Optional.empty());

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCliente()).isNull();
    }

    @Test
    @DisplayName("Debe manejar evento no encontrado")
    void testEventoNoEncontrado() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setValorOrden(150000.0);
        event.setEventoId(999L);
        event.setClienteId("12345678");

        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEvento()).isNull();
        assertThat(result.getCliente()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar tarifa no encontrada")
    void testTarifaNoEncontrada() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setValorOrden(150000.0);
        event.setTarifaId(999L);
        event.setClienteId("12345678");

        when(tarifaRepository.findById(999L)).thenReturn(Optional.empty());

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTarifa()).isNull();
        assertThat(result.getCliente()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar tickets no encontrados")
    void testTicketsNoEncontrados() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setValorOrden(150000.0);
        event.setTicketsIds(Arrays.asList(999L, 888L));
        event.setClienteId("12345678");

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        when(ticketRepository.findById(888L)).thenReturn(Optional.empty());

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTickets()).isNotNull();
        // Si el adapter agrega tickets null cuando no los encuentra, entonces size = 2
        // Si el adapter filtra tickets null, entonces size = 0
        // Necesitamos verificar cuál es el comportamiento real del adapter
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getTickets()).allMatch(ticket -> ticket == null);
    }

    @Test
    @DisplayName("Debe manejar valores monetarios decimales")
    void testValoresMonetariosDecimales() {
        // Arrange
        OrdenEvent event = new OrdenEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(1);
        event.setValorOrden(123456.78);
        event.setValorSeguro(9876.54);
        event.setClienteId("12345678");

        Orden orden = new Orden();

        // Act
        Orden result = adapter.creacion(orden, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getValorOrden()).isEqualTo(123456.78);
        assertThat(result.getValorSeguro()).isEqualTo(9876.54);
    }
}
