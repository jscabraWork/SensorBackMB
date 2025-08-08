package com.arquitectura.orden.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;

class OrdenCreationHelperTest {

    @InjectMocks
    private OrdenCreationHelper ordenCreationHelper;

    @Mock
    private ClienteService clienteService;

    @Mock
    private EventoService eventoService;

    @Mock
    private LocalidadService localidadService;

    @Mock
    private TicketService ticketService;

    private Cliente cliente;
    private Evento evento;
    private Localidad localidad;
    private List<Ticket> tickets;
    private Ticket ticketPadre;
    private List<Ticket> ticketsHijos;

    // Orden de prueba que extiende de Orden
    private static class TestOrden extends Orden {
        public TestOrden(Evento evento, Cliente cliente, List<Ticket> tickets) {
            // Constructor básico para pruebas
            this.setEvento(evento);
            this.setCliente(cliente);
            this.setTickets(tickets);
        }
    }

    // Factory para crear TestOrden
    private OrdenCreationHelper.OrdenFactory<TestOrden> testOrdenFactory = TestOrden::new;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar entidades de prueba
        cliente = new Cliente();
        cliente.setNumeroDocumento("12345678");
        cliente.setNombre("Cliente Test");

        evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Evento Test");

        localidad = new Localidad();
        localidad.setId(1L);
        localidad.setNombre("Localidad Test");

        // Configurar tickets disponibles
        tickets = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Ticket ticket = new Ticket();
            ticket.setId((long) i);
            ticket.setEstado(0); // DISPONIBLE
            ticket.setLocalidad(localidad);
            tickets.add(ticket);
        }

        // Configurar ticket padre y sus hijos para pruebas de palco
        ticketPadre = new Ticket();
        ticketPadre.setId(100L);
        ticketPadre.setEstado(0); // DISPONIBLE
        ticketPadre.setLocalidad(localidad);

        ticketsHijos = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Ticket hijo = new Ticket();
            hijo.setId(100L + i);
            hijo.setEstado(0); // DISPONIBLE
            hijo.setLocalidad(localidad);
            ticketsHijos.add(hijo);
        }
        ticketPadre.setAsientos(ticketsHijos);
    }

    @Nested
    @DisplayName("Crear Orden No Numerada")
    class CrearOrdenNoNumeradaTest {

        @Test
        @DisplayName("Debe crear orden no numerada exitosamente")
        void debeCrearOrdenNoNumeradaExitosamente() throws Exception {
            // Arrange
            when(localidadService.findById(1L)).thenReturn(localidad);
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findTicketsByLocalidadIdAndEstado(1L, 0, 3)).thenReturn(tickets.subList(0, 3));

            // Act
            TestOrden orden = ordenCreationHelper.crearOrdenNoNumerada(3, 1L, "12345678", 1L, testOrdenFactory);

            // Assert
            assertThat(orden).isNotNull();
            assertThat(orden.getEvento()).isEqualTo(evento);
            assertThat(orden.getCliente()).isEqualTo(cliente);
            assertThat(orden.getTickets()).hasSize(3);
        }

        @Test
        @DisplayName("Debe fallar cuando la localidad no existe")
        void debeFallarCuandoLocalidadNoExiste() {
            // Arrange
            when(localidadService.findById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNoNumerada(3, 1L, "12345678", 1L, testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando el cliente no existe")
        void debeFallarCuandoClienteNoExiste() {
            // Arrange
            when(localidadService.findById(1L)).thenReturn(localidad);
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNoNumerada(3, 1L, "12345678", 1L, testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando el evento no existe")
        void debeFallarCuandoEventoNoExiste() {
            // Arrange
            when(localidadService.findById(1L)).thenReturn(localidad);
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNoNumerada(3, 1L, "12345678", 1L, testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando no hay suficientes tickets disponibles")
        void debeFallarCuandoNoHaySuficientesTickets() {
            // Arrange
            when(localidadService.findById(1L)).thenReturn(localidad);
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findTicketsByLocalidadIdAndEstado(1L, 0, 10)).thenReturn(tickets.subList(0, 3)); // Solo 3 tickets disponibles

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNoNumerada(10, 1L, "12345678", 1L, testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Crear Orden Numerada")
    class CrearOrdenNumeradaTest {

        @Test
        @DisplayName("Debe crear orden numerada exitosamente")
        void debeCrearOrdenNumeradaExitosamente() throws Exception {
            // Arrange
            List<Ticket> ticketsEnProceso = tickets.subList(0, 3);
            ticketsEnProceso.forEach(t -> t.setEstado(3)); // EN PROCESO

            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findAllById(anyList())).thenReturn(ticketsEnProceso);

            // Act
            TestOrden orden = ordenCreationHelper.crearOrdenNumerada(ticketsEnProceso, 1L, "12345678", testOrdenFactory);

            // Assert
            assertThat(orden).isNotNull();
            assertThat(orden.getEvento()).isEqualTo(evento);
            assertThat(orden.getCliente()).isEqualTo(cliente);
            assertThat(orden.getTickets()).hasSize(3);
        }

        @Test
        @DisplayName("Debe fallar cuando el cliente no existe")
        void debeFallarCuandoClienteNoExiste() {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNumerada(tickets.subList(0, 3), 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando el evento no existe")
        void debeFallarCuandoEventoNoExiste() {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNumerada(tickets.subList(0, 3), 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando los tickets pertenecen a diferentes localidades")
        void debeFallarCuandoTicketsDiferentesLocalidades() {
            // Arrange
            Localidad otraLocalidad = new Localidad();
            otraLocalidad.setId(2L);
            otraLocalidad.setNombre("Otra Localidad");

            List<Ticket> ticketsMixtos = Arrays.asList(tickets.get(0), tickets.get(1));
            ticketsMixtos.get(1).setLocalidad(otraLocalidad); // Diferente localidad

            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findAllById(anyList())).thenReturn(ticketsMixtos);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNumerada(ticketsMixtos, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Debe fallar cuando los tickets no están en proceso")
        void debeFallarCuandoTicketsNoEnProceso() {
            // Arrange
            List<Ticket> ticketsNoDisponibles = tickets.subList(0, 3);
            ticketsNoDisponibles.forEach(t -> t.setEstado(1)); // VENDIDO

            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findAllById(anyList())).thenReturn(ticketsNoDisponibles);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNumerada(ticketsNoDisponibles, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Crear Orden Palco Individual")
    class CrearOrdenPalcoIndividualTest {

        @Test
        @DisplayName("Debe crear orden con solo tickets hijos cuando hay suficientes")
        void debeCrearOrdenConSoloHijos() throws Exception {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findById(100L)).thenReturn(ticketPadre);

            // Act
            TestOrden orden = ordenCreationHelper.crearOrdenPalcoIndividual(100L, 2, 1L, "12345678", testOrdenFactory);

            // Assert
            assertThat(orden).isNotNull();
            assertThat(orden.getEvento()).isEqualTo(evento);
            assertThat(orden.getCliente()).isEqualTo(cliente);
            assertThat(orden.getTickets()).hasSize(2);
            assertThat(orden.getTickets()).allMatch(t -> ticketsHijos.contains(t));
            assertThat(orden.getTickets()).doesNotContain(ticketPadre);
        }

        @Test
        @DisplayName("Debe crear orden con hijos + padre cuando no hay suficientes hijos")
        void debeCrearOrdenConHijosYPadre() throws Exception {
            // Arrange - Solo 2 hijos disponibles pero se solicitan 3 tickets
            ticketsHijos.get(2).setEstado(1); // Hacer el tercer hijo no disponible

            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findById(100L)).thenReturn(ticketPadre);

            // Act
            TestOrden orden = ordenCreationHelper.crearOrdenPalcoIndividual(100L, 3, 1L, "12345678", testOrdenFactory);

            // Assert
            assertThat(orden).isNotNull();
            assertThat(orden.getEvento()).isEqualTo(evento);
            assertThat(orden.getCliente()).isEqualTo(cliente);
            assertThat(orden.getTickets()).hasSize(3);
            assertThat(orden.getTickets()).contains(ticketPadre);
            assertThat(orden.getTickets()).contains(ticketsHijos.get(0), ticketsHijos.get(1));
        }

        @Test
        @DisplayName("Debe fallar cuando el cliente no existe")
        void debeFallarCuandoClienteNoExiste() {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenPalcoIndividual(100L, 2, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando el evento no existe")
        void debeFallarCuandoEventoNoExiste() {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenPalcoIndividual(100L, 2, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando el ticket padre no existe")
        void debeFallarCuandoTicketPadreNoExiste() {
            // Arrange
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findById(100L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenPalcoIndividual(100L, 2, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Debe fallar cuando no hay suficiente capacidad total")
        void debeFallarCuandoNoHaySuficienteCapacidad() {
            // Arrange - Solo 1 hijo disponible y padre no disponible
            ticketsHijos.forEach(t -> t.setEstado(1)); // Todos los hijos vendidos
            ticketsHijos.get(0).setEstado(0); // Solo 1 hijo disponible
            ticketPadre.setEstado(1); // Padre no disponible

            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findById(100L)).thenReturn(ticketPadre);

            // Act & Assert - Solicitar 3 tickets pero solo hay 1 disponible
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenPalcoIndividual(100L, 3, 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Debe manejar caso límite donde se necesitan todos los tickets disponibles")
        void debeManejarCasoLimiteConTodosLosTickets() throws Exception {
            // Arrange - Exactamente 4 tickets disponibles (3 hijos + 1 padre)
            when(clienteService.findByNumeroDocumento("12345678")).thenReturn(cliente);
            when(eventoService.findById(1L)).thenReturn(evento);
            when(ticketService.findById(100L)).thenReturn(ticketPadre);

            // Act - Solicitar exactamente 4 tickets
            TestOrden orden = ordenCreationHelper.crearOrdenPalcoIndividual(100L, 4, 1L, "12345678", testOrdenFactory);

            // Assert
            assertThat(orden).isNotNull();
            assertThat(orden.getTickets()).hasSize(4);
            assertThat(orden.getTickets()).contains(ticketPadre);
            assertThat(orden.getTickets()).containsAll(ticketsHijos);
        }
    }

    @Nested
    @DisplayName("Casos de Error Generales")
    class CasosErrorGeneralesTest {

        @Test
        @DisplayName("Debe propagar excepciones no controladas como errores internos")
        void debePropagaExcepcionesNoControladas() {
            // Arrange
            when(clienteService.findByNumeroDocumento(anyString())).thenThrow(new RuntimeException("Error inesperado"));

            // Act & Assert
            assertThatThrownBy(() ->
                ordenCreationHelper.crearOrdenNumerada(tickets.subList(0, 2), 1L, "12345678", testOrdenFactory)
            )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}