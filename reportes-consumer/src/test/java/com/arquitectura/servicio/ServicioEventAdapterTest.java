package com.arquitectura.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.arquitectura.events.ServicioEvent;
import com.arquitectura.servicio.consumer.ServicioEventAdapterImpl;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class ServicioEventAdapterTest {

    @InjectMocks
    private ServicioEventAdapterImpl adapter;

    @Mock
    private TicketRepository ticketRepository;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear entidad Ticket para los tests
        ticket = new Ticket();
        ticket.setId(100L);

        // Configurar comportamiento del mock
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
    }

    @Test
    @DisplayName("Debe convertir correctamente ServicioEvent a Servicio para creación")
    void testCreacionDesdeServicioEvent() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP");
        event.setUtilizado(false);
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Parqueadero VIP");
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getTicket()).isNotNull();
        assertThat(result.getTicket().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Servicio existente")
    void testActualizacionServicioExistente() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP - Actualizado");
        event.setUtilizado(true);
        event.setTicketId(100L);

        Servicio servicioExistente = new Servicio();
        servicioExistente.setId(1L);
        servicioExistente.setNombre("Parqueadero VIP");
        servicioExistente.setUtilizado(false);

        // Act
        Servicio result = adapter.creacion(servicioExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Parqueadero VIP - Actualizado");
        assertThat(result.isUtilizado()).isTrue();
        assertThat(result.getTicket()).isNotNull();
        assertThat(result.getTicket().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre(null);
        event.setUtilizado(false);
        event.setTicketId(null);

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isNull();
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getTicket()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP");
        event.setUtilizado(false);
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isSameAs(servicio);
    }

    @Test
    @DisplayName("Debe manejar ticket no encontrado")
    void testTicketNoEncontrado() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP");
        event.setUtilizado(false);
        event.setTicketId(999L);

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Parqueadero VIP");
        assertThat(result.isUtilizado()).isFalse();
        assertThat(result.getTicket()).isNull();
    }

    @Test
    @DisplayName("Debe manejar correctamente diferentes tipos de servicios")
    void testDiferentesTiposServicios() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setUtilizado(false);
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Test Parqueadero
        event.setNombre("Parqueadero VIP");
        Servicio result1 = adapter.creacion(servicio, event);
        assertThat(result1.getNombre()).isEqualTo("Parqueadero VIP");

        // Test Comida
        event.setNombre("Menú Ejecutivo");
        Servicio result2 = adapter.creacion(servicio, event);
        assertThat(result2.getNombre()).isEqualTo("Menú Ejecutivo");

        // Test Bebida
        event.setNombre("Bebida Premium");
        Servicio result3 = adapter.creacion(servicio, event);
        assertThat(result3.getNombre()).isEqualTo("Bebida Premium");

        // Test Merchandising
        event.setNombre("Camiseta del Evento");
        Servicio result4 = adapter.creacion(servicio, event);
        assertThat(result4.getNombre()).isEqualTo("Camiseta del Evento");
    }

    @Test
    @DisplayName("Debe manejar correctamente el estado utilizado")
    void testEstadoUtilizado() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP");
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Test utilizado = false
        event.setUtilizado(false);
        Servicio result1 = adapter.creacion(servicio, event);
        assertThat(result1.isUtilizado()).isFalse();

        // Test utilizado = true
        event.setUtilizado(true);
        Servicio result2 = adapter.creacion(servicio, event);
        assertThat(result2.isUtilizado()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar nombres de servicios largos")
    void testNombresLargos() {
        // Arrange
        String nombreLargo = "Servicio Premium de Parqueadero VIP con Asistencia Personalizada y Valet Parking";
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre(nombreLargo);
        event.setUtilizado(false);
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo(nombreLargo);
    }

    @Test
    @DisplayName("Debe manejar servicios con caracteres especiales")
    void testNombresConCaracteresEspeciales() {
        // Arrange
        String nombreEspecial = "Menú Á-la-Carte & Bebidas 100% Naturales";
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre(nombreEspecial);
        event.setUtilizado(false);
        event.setTicketId(100L);

        Servicio servicio = new Servicio();

        // Act
        Servicio result = adapter.creacion(servicio, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo(nombreEspecial);
    }

    @Test
    @DisplayName("Debe manejar cambio de estado de utilizado")
    void testCambioEstadoUtilizado() {
        // Arrange
        ServicioEvent event = new ServicioEvent();
        event.setId(1L);
        event.setNombre("Parqueadero VIP");
        event.setUtilizado(true);
        event.setTicketId(100L);

        Servicio servicioExistente = new Servicio();
        servicioExistente.setId(1L);
        servicioExistente.setNombre("Parqueadero VIP");
        servicioExistente.setUtilizado(false); // Estaba sin utilizar

        // Act
        Servicio result = adapter.creacion(servicioExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUtilizado()).isTrue(); // Ahora está utilizado
    }
}
