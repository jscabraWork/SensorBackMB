package com.arquitectura.orden_alcancia;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenAlcanciaEvent;
import com.arquitectura.orden.orden_alcancia.OrdenAlcanciaEventAdapterImpl;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
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

public class OrdenAlcanciaEventAdapterTest {

    @InjectMocks
    private OrdenAlcanciaEventAdapterImpl adapter;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private TarifaRepository tarifaRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AlcanciaRepository alcanciaRepository;

    private Cliente cliente;
    private Evento evento;
    private Tarifa tarifa;
    private Ticket ticket1;
    private Ticket ticket2;
    private Alcancia alcancia;

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

        alcancia = new Alcancia();
        alcancia.setId(50L);
        alcancia.setPrecioTotal(200000.0);
        alcancia.setActiva(true);

        // Configurar comportamiento de los mocks
        when(clienteRepository.findById("12345678")).thenReturn(Optional.of(cliente));
        when(eventoRepository.findById(100L)).thenReturn(Optional.of(evento));
        when(tarifaRepository.findById(10L)).thenReturn(Optional.of(tarifa));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
        when(alcanciaRepository.findById(50L)).thenReturn(Optional.of(alcancia));
    }

    @Test
    @DisplayName("Debe convertir correctamente OrdenAlcanciaEvent a OrdenAlcancia para creación")
    void testCreacionDesdeOrdenAlcanciaEvent() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1); // ACEPTADA
        event.setTipo(3); // CREAR ALCANCIA
        event.setEventoId(100L);
        event.setValorOrden(150000.0);
        event.setValorSeguro(5000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setTarifaId(10L);
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(3);
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
        assertThat(result.getAlcancia()).isNotNull();
        assertThat(result.getAlcancia().getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una OrdenAlcancia existente")
    void testActualizacionOrdenAlcanciaExistente() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1); // ACEPTADA
        event.setTipo(4); // APORTAR A ALCANCIA
        event.setEventoId(100L);
        event.setValorOrden(250000.0);
        event.setValorSeguro(8000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setTarifaId(10L);
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcanciaExistente = new OrdenAlcancia();
        ordenAlcanciaExistente.setId(1L);
        ordenAlcanciaExistente.setEstado(3);
        ordenAlcanciaExistente.setTipo(3);
        ordenAlcanciaExistente.setValorOrden(150000.0);
        ordenAlcanciaExistente.setValorSeguro(5000.0);

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcanciaExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(4);
        assertThat(result.getValorOrden()).isEqualTo(250000.0);
        assertThat(result.getValorSeguro()).isEqualTo(8000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getAlcancia()).isNotNull();
        assertThat(result.getAlcancia().getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(null);
        event.setTipo(null);
        event.setEventoId(null);
        event.setValorOrden(null);
        event.setValorSeguro(null);
        event.setTicketsIds(null);
        event.setClienteId(null);
        event.setTarifaId(null);
        event.setAlcanciaId(null);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());
        when(eventoRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(tarifaRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(alcanciaRepository.findById(anyLong())).thenReturn(Optional.empty());

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

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
        assertThat(result.getAlcancia()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setClienteId("12345678");
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isSameAs(ordenAlcancia);
    }

    @Test
    @DisplayName("Debe manejar tipos específicos de alcancía")
    void testTiposEspecificosAlcancia() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setValorOrden(150000.0);
        event.setClienteId("12345678");
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Test CREAR ALCANCIA
        event.setTipo(3);
        OrdenAlcancia result1 = adapter.creacion(ordenAlcancia, event);
        assertThat(result1.getTipo()).isEqualTo(3);

        // Test APORTAR A ALCANCIA
        event.setTipo(4);
        OrdenAlcancia result2 = adapter.creacion(ordenAlcancia, event);
        assertThat(result2.getTipo()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe manejar alcancía no encontrada")
    void testAlcanciaNoEncontrada() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setValorOrden(150000.0);
        event.setAlcanciaId(999L);
        event.setClienteId("12345678");

        when(alcanciaRepository.findById(999L)).thenReturn(Optional.empty());

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAlcancia()).isNull();
        assertThat(result.getCliente()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar herencia de campos de orden base")
    void testHerenciaCamposOrdenBase() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setEventoId(100L);
        event.setValorOrden(150000.0);
        event.setValorSeguro(5000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setTarifaId(10L);
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert - Verificar campos heredados de Orden
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(3);
        assertThat(result.getValorOrden()).isEqualTo(150000.0);
        assertThat(result.getValorSeguro()).isEqualTo(5000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTickets()).isNotNull();

        // Verificar campo específico de OrdenAlcancia
        assertThat(result.getAlcancia()).isNotNull();
        assertThat(result.getAlcancia().getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Debe manejar correctamente valores monetarios grandes")
    void testValoresMonetariosGrandes() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setValorOrden(1000000.0); // 1 millón
        event.setValorSeguro(50000.0);   // 50 mil
        event.setClienteId("12345678");
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getValorOrden()).isEqualTo(1000000.0);
        assertThat(result.getValorSeguro()).isEqualTo(50000.0);
    }

    @Test
    @DisplayName("Debe manejar correctamente múltiples tickets")
    void testMultiplesTickets() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setValorOrden(150000.0);
        event.setTicketsIds(Arrays.asList(1L, 2L));
        event.setClienteId("12345678");
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getTickets().get(0).getId()).isEqualTo(1L);
        assertThat(result.getTickets().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Debe manejar lista de tickets vacía")
    void testListaTicketsVacia() {
        // Arrange
        OrdenAlcanciaEvent event = new OrdenAlcanciaEvent();
        event.setId(1L);
        event.setEstado(1);
        event.setTipo(3);
        event.setValorOrden(150000.0);
        event.setTicketsIds(Arrays.asList());
        event.setClienteId("12345678");
        event.setAlcanciaId(50L);

        OrdenAlcancia ordenAlcancia = new OrdenAlcancia();

        // Act
        OrdenAlcancia result = adapter.creacion(ordenAlcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).isEmpty();
    }
}
