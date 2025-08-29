package com.arquitectura.orden_puntofisico;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
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

public class OrdenPuntoFisicoEventAdapterTest {

    @InjectMocks
    private OrdenPuntoFisicoEventAdapterImpl adapter;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private TarifaRepository tarifaRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PuntoFisicoRepository puntoFisicoRepository;

    private Cliente cliente;
    private Evento evento;
    private Tarifa tarifa;
    private Ticket ticket1;
    private Ticket ticket2;
    private PuntoFisico puntoFisico;

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

        puntoFisico = new PuntoFisico();
        puntoFisico.setNumeroDocumento("PF001");
        puntoFisico.setNombre("Punto Físico de prueba");

        // Configurar comportamiento de los mocks
        when(clienteRepository.findById("12345678")).thenReturn(Optional.of(cliente));
        when(eventoRepository.findById(100L)).thenReturn(Optional.of(evento));
        when(tarifaRepository.findById(10L)).thenReturn(Optional.of(tarifa));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
        when(puntoFisicoRepository.findById("PF001")).thenReturn(Optional.of(puntoFisico));
    }

    @Test
    @DisplayName("Debe convertir correctamente OrdenPuntoFisicoEvent a OrdenPuntoFisico para creación")
    void testCreacionDesdeOrdenPuntoFisicoEvent() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado: ACEPTADA
                7,            // tipo: VENTA PUNTO FISICO
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(7);
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
        assertThat(result.getPuntoFisico()).isNotNull();
        assertThat(result.getPuntoFisico().getNumeroDocumento()).isEqualTo("PF001");
    }

    @Test
    @DisplayName("Debe actualizar correctamente una OrdenPuntoFisico existente")
    void testActualizacionOrdenPuntoFisicoExistente() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                2,            // estado: RECHAZADA
                8,            // tipo: DEVOLUCION PUNTO FISICO
                100L,         // eventoId
                250000.0,     // valorOrden
                8000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisicoExistente = new OrdenPuntoFisico();
        ordenPuntoFisicoExistente.setId(1L);
        ordenPuntoFisicoExistente.setEstado(1);
        ordenPuntoFisicoExistente.setTipo(7);
        ordenPuntoFisicoExistente.setValorOrden(150000.0);
        ordenPuntoFisicoExistente.setValorSeguro(5000.0);

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisicoExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(2);
        assertThat(result.getTipo()).isEqualTo(8);
        assertThat(result.getValorOrden()).isEqualTo(250000.0);
        assertThat(result.getValorSeguro()).isEqualTo(8000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getPuntoFisico()).isNotNull();
        assertThat(result.getPuntoFisico().getNumeroDocumento()).isEqualTo("PF001");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,    // id
                null,  // estado
                null,  // tipo
                null,  // eventoId
                null,  // valorOrden
                null,  // valorSeguro
                null,  // ticketsIds
                null,  // clienteId
                null,  // tarifaId
                null   // puntoFisicoId
        );

        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());
        when(eventoRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(tarifaRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(puntoFisicoRepository.findById(anyString())).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

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
        assertThat(result.getPuntoFisico()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isSameAs(ordenPuntoFisico);
    }

    @Test
    @DisplayName("Debe manejar tipos específicos de punto físico")
    void testTiposEspecificosPuntoFisico() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                null,         // tipo - se establece en cada test
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Test VENTA PUNTO FISICO
        event.setTipo(7);
        OrdenPuntoFisico result1 = adapter.creacion(ordenPuntoFisico, event);
        assertThat(result1.getTipo()).isEqualTo(7);

        // Test DEVOLUCION PUNTO FISICO
        event.setTipo(8);
        OrdenPuntoFisico result2 = adapter.creacion(ordenPuntoFisico, event);
        assertThat(result2.getTipo()).isEqualTo(8);
    }

    @Test
    @DisplayName("Debe manejar punto físico no encontrado")
    void testPuntoFisicoNoEncontrado() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF999"       // puntoFisicoId inexistente
        );

        when(puntoFisicoRepository.findById("PF999")).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPuntoFisico()).isNull();
        assertThat(result.getCliente()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar cliente no encontrado")
    void testClienteNoEncontrado() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "99999999",   // clienteId inexistente
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        when(clienteRepository.findById("99999999")).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCliente()).isNull();
        assertThat(result.getPuntoFisico()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar evento no encontrado")
    void testEventoNoEncontrado() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                999L,         // eventoId inexistente
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEvento()).isNull();
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getPuntoFisico()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar tarifa no encontrada")
    void testTarifaNoEncontrada() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                999L,         // tarifaId inexistente
                "PF001"       // puntoFisicoId
        );

        when(tarifaRepository.findById(999L)).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTarifa()).isNull();
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getPuntoFisico()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar tickets no encontrados")
    void testTicketsNoEncontrados() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(999L, 888L), // ticketsIds inexistentes
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        when(ticketRepository.findById(888L)).thenReturn(Optional.empty());

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).hasSize(2);
        assertThat(result.getTickets()).allMatch(ticket -> ticket == null);
    }

    @Test
    @DisplayName("Debe manejar valores monetarios decimales")
    void testValoresMonetariosDecimales() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                123456.78,    // valorOrden
                9876.54,      // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getValorOrden()).isEqualTo(123456.78);
        assertThat(result.getValorSeguro()).isEqualTo(9876.54);
    }

    @Test
    @DisplayName("Debe manejar herencia de campos de orden base")
    void testHerenciaCamposOrdenBase() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert - Verificar campos heredados de Orden
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(1);
        assertThat(result.getTipo()).isEqualTo(7);
        assertThat(result.getValorOrden()).isEqualTo(150000.0);
        assertThat(result.getValorSeguro()).isEqualTo(5000.0);
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getEvento()).isNotNull();
        assertThat(result.getTarifa()).isNotNull();
        assertThat(result.getTickets()).isNotNull();

        // Verificar campo específico de OrdenPuntoFisico
        assertThat(result.getPuntoFisico()).isNotNull();
        assertThat(result.getPuntoFisico().getNumeroDocumento()).isEqualTo("PF001");
    }

    @Test
    @DisplayName("Debe manejar correctamente múltiples tickets")
    void testMultiplesTickets() {
        // Arrange
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(1L, 2L), // ticketsIds
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

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
        OrdenPuntoFisicoEvent event = new OrdenPuntoFisicoEvent(
                1L,           // id
                1,            // estado
                7,            // tipo
                100L,         // eventoId
                150000.0,     // valorOrden
                5000.0,       // valorSeguro
                Arrays.asList(), // ticketsIds vacía
                "12345678",   // clienteId
                10L,          // tarifaId
                "PF001"       // puntoFisicoId
        );

        OrdenPuntoFisico ordenPuntoFisico = new OrdenPuntoFisico();

        // Act
        OrdenPuntoFisico result = adapter.creacion(ordenPuntoFisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTickets()).isNotNull();
        assertThat(result.getTickets()).isEmpty();
    }
}