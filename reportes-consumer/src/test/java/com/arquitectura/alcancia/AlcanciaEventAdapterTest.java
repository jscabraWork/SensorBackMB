package com.arquitectura.alcancia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.arquitectura.alcancia.consumer.AlcanciaEventAdapterImpl;
import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.events.AlcanciaEvent;
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

public class AlcanciaEventAdapterTest {

    @InjectMocks
    private AlcanciaEventAdapterImpl adapter;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TicketRepository ticketRepository;

    private Cliente cliente;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear entidades Cliente y Ticket para los tests
        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Juan Pérez");

        ticket1 = new Ticket();
        ticket1.setId(1L);

        ticket2 = new Ticket();
        ticket2.setId(2L);

        // Configurar comportamiento de los mocks
        when(clienteRepository.findById("12345678")).thenReturn(Optional.of(cliente));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
    }

    @Test
    @DisplayName("Debe convertir correctamente AlcanciaEvent a Alcancia para creación")
    void testCreacionDesdeAlcanciaEvent() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(50000.0);
        event.setPrecioTotal(100000.0);
        event.setActiva(true);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList(1L, 2L));

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrecioParcialPagado()).isEqualTo(50000.0);
        assertThat(result.getPrecioTotal()).isEqualTo(100000.0);
        assertThat(result.isActiva()).isTrue();
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getCliente().getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Alcancia existente")
    void testActualizacionAlcanciaExistente() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(75000.0);
        event.setPrecioTotal(150000.0);
        event.setActiva(false);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList(1L, 2L));

        Alcancia alcanciaExistente = new Alcancia();
        alcanciaExistente.setId(1L);
        alcanciaExistente.setPrecioParcialPagado(50000.0);
        alcanciaExistente.setPrecioTotal(100000.0);
        alcanciaExistente.setActiva(true);

        // Act
        Alcancia result = adapter.creacion(alcanciaExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrecioParcialPagado()).isEqualTo(75000.0);
        assertThat(result.getPrecioTotal()).isEqualTo(150000.0);
        assertThat(result.isActiva()).isFalse();
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getCliente().getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(null);
        event.setPrecioTotal(null);
        event.setActiva(false);
        event.setClienteNumeroDocumento(null);
        event.setTicketsIds(null);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrecioParcialPagado()).isNull();
        assertThat(result.getPrecioTotal()).isNull();
        assertThat(result.isActiva()).isFalse();
        assertThat(result.getCliente()).isNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).isEmpty();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(50000.0);
        event.setPrecioTotal(100000.0);
        event.setActiva(true);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList(1L));

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isSameAs(alcancia);
    }

    @Test
    @DisplayName("Debe manejar cliente no encontrado")
    void testClienteNoEncontrado() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(50000.0);
        event.setPrecioTotal(100000.0);
        event.setActiva(true);
        event.setClienteNumeroDocumento("99999999");
        event.setTicketsIds(Arrays.asList(1L));

        when(clienteRepository.findById("99999999")).thenReturn(Optional.empty());

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCliente()).isNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(1);
    }

    @Test
    @DisplayName("Debe manejar tickets no encontrados")
    void testTicketsNoEncontrados() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(50000.0);
        event.setPrecioTotal(100000.0);
        event.setActiva(true);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList(999L, 888L));

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        when(ticketRepository.findById(888L)).thenReturn(Optional.empty());

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getTickets()).allMatch(ticket -> ticket == null);
    }

    @Test
    @DisplayName("Debe manejar lista de tickets vacía")
    void testListaTicketsVacia() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(50000.0);
        event.setPrecioTotal(100000.0);
        event.setActiva(true);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList());

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).isEmpty();
    }

    @Test
    @DisplayName("Debe manejar correctamente precios con decimales")
    void testPreciosConDecimales() {
        // Arrange
        AlcanciaEvent event = new AlcanciaEvent();
        event.setId(1L);
        event.setPrecioParcialPagado(12345.67);
        event.setPrecioTotal(98765.43);
        event.setActiva(true);
        event.setClienteNumeroDocumento("12345678");
        event.setTicketsIds(Arrays.asList(1L));

        Alcancia alcancia = new Alcancia();

        // Act
        Alcancia result = adapter.creacion(alcancia, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrecioParcialPagado()).isEqualTo(12345.67);
        assertThat(result.getPrecioTotal()).isEqualTo(98765.43);
        assertThat(result.isActiva()).isTrue();
    }
}
