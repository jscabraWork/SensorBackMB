package com.arquitectura.transaccion;

import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.transaccion.consumer.TransaccionEventAdapterImpl;
import com.arquitectura.transaccion.entity.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TransaccionEventAdapterTest {

    @InjectMocks
    private TransaccionEventAdapterImpl adapter;

    @Mock
    private OrdenRepository ordenRepository;

    private Orden orden;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear entidad Orden para los tests
        orden = new Orden();
        orden.setId(100L);

        // Configurar comportamiento del mock
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(orden));
    }

    @Test
    @DisplayName("Debe convertir correctamente TransaccionEvent a Transaccion para creación")
    void testCreacionDesdeTransaccionEvent() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail("test@example.com");
        event.setFullname("Juan Pérez");
        event.setIdPasarela("PASARELA123");
        event.setIdPersona("12345678");
        event.setIp("192.168.1.1");
        event.setMetodo(1); // TARJETA CREDITO
        event.setMetodoNombre("Tarjeta de Crédito");
        event.setPhone("3001234567");
        event.setStatus(34); // APROBADA
        event.setIdBasePasarela("BASE123");
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(150000.0);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFullName()).isEqualTo("Juan Pérez");
        assertThat(result.getIdPasarela()).isEqualTo("PASARELA123");
        assertThat(result.getIdPersona()).isEqualTo("12345678");
        assertThat(result.getIp()).isEqualTo("192.168.1.1");
        assertThat(result.getMetodo()).isEqualTo(1);
        assertThat(result.getMetodoNombre()).isEqualTo("Tarjeta de Crédito");
        assertThat(result.getPhone()).isEqualTo("3001234567");
        assertThat(result.getStatus()).isEqualTo(34);
        assertThat(result.getIdBasePasarela()).isEqualTo("BASE123");
        assertThat(result.getOrden()).isNotNull();
        assertThat(result.getOrden().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una Transacción existente")
    void testActualizacionTransaccionExistente() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(200000.0);
        event.setEmail("updated@example.com");
        event.setFullname("Juan Pérez Actualizado");
        event.setIdPasarela("PASARELA456");
        event.setIdPersona("87654321");
        event.setIp("192.168.1.2");
        event.setMetodo(2); // PSE
        event.setMetodoNombre("PSE");
        event.setPhone("3007654321");
        event.setStatus(35); // EN PROCESO
        event.setIdBasePasarela("BASE456");
        event.setOrdenId(100L);

        Transaccion transaccionExistente = new Transaccion();
        transaccionExistente.setId(1L);
        transaccionExistente.setAmount(150000.0);
        transaccionExistente.setEmail("test@example.com");
        transaccionExistente.setStatus(34);

        // Act
        Transaccion result = adapter.creacion(transaccionExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(200000.0);
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getFullName()).isEqualTo("Juan Pérez Actualizado");
        assertThat(result.getIdPasarela()).isEqualTo("PASARELA456");
        assertThat(result.getIdPersona()).isEqualTo("87654321");
        assertThat(result.getIp()).isEqualTo("192.168.1.2");
        assertThat(result.getMetodo()).isEqualTo(2);
        assertThat(result.getMetodoNombre()).isEqualTo("PSE");
        assertThat(result.getPhone()).isEqualTo("3007654321");
        assertThat(result.getStatus()).isEqualTo(35);
        assertThat(result.getIdBasePasarela()).isEqualTo("BASE456");
        assertThat(result.getOrden()).isNotNull();
        assertThat(result.getOrden().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(null);
        event.setEmail(null);
        event.setFullname(null);
        event.setIdPasarela(null);
        event.setIdPersona(null);
        event.setIp(null);
        event.setMetodo(0);
        event.setMetodoNombre(null);
        event.setPhone(null);
        event.setStatus(0);
        event.setIdBasePasarela(null);
        event.setOrdenId(null);

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getFullName()).isNull();
        assertThat(result.getIdPasarela()).isNull();
        assertThat(result.getIdPersona()).isNull();
        assertThat(result.getIp()).isNull();
        assertThat(result.getMetodo()).isEqualTo(0);
        assertThat(result.getMetodoNombre()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getIdBasePasarela()).isNull();
        assertThat(result.getOrden()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail("test@example.com");
        event.setStatus(34);
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isSameAs(transaccion);
    }

    @Test
    @DisplayName("Debe manejar orden no encontrada")
    void testOrdenNoEncontrada() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail("test@example.com");
        event.setStatus(34);
        event.setOrdenId(999L);

        when(ordenRepository.findById(999L)).thenReturn(Optional.empty());

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(150000.0);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getStatus()).isEqualTo(34);
        assertThat(result.getOrden()).isNull();
    }

    @Test
    @DisplayName("Debe manejar correctamente diferentes métodos de pago")
    void testDiferentesMetodosPago() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail("test@example.com");
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Test TARJETA CREDITO
        event.setMetodo(1);
        event.setMetodoNombre("Tarjeta de Crédito");
        Transaccion result1 = adapter.creacion(transaccion, event);
        assertThat(result1.getMetodo()).isEqualTo(1);
        assertThat(result1.getMetodoNombre()).isEqualTo("Tarjeta de Crédito");

        // Test PSE
        event.setMetodo(2);
        event.setMetodoNombre("PSE");
        Transaccion result2 = adapter.creacion(transaccion, event);
        assertThat(result2.getMetodo()).isEqualTo(2);
        assertThat(result2.getMetodoNombre()).isEqualTo("PSE");

        // Test EFECTIVO
        event.setMetodo(4);
        event.setMetodoNombre("Efectivo");
        Transaccion result3 = adapter.creacion(transaccion, event);
        assertThat(result3.getMetodo()).isEqualTo(4);
        assertThat(result3.getMetodoNombre()).isEqualTo("Efectivo");
    }

    @Test
    @DisplayName("Debe manejar correctamente diferentes estados de transacción")
    void testDiferentesEstadosTransaccion() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail("test@example.com");
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Test APROBADA
        event.setStatus(34);
        Transaccion result1 = adapter.creacion(transaccion, event);
        assertThat(result1.getStatus()).isEqualTo(34);

        // Test EN PROCESO
        event.setStatus(35);
        Transaccion result2 = adapter.creacion(transaccion, event);
        assertThat(result2.getStatus()).isEqualTo(35);

        // Test RECHAZADA
        event.setStatus(36);
        Transaccion result3 = adapter.creacion(transaccion, event);
        assertThat(result3.getStatus()).isEqualTo(36);

        // Test DEVOLUCION
        event.setStatus(4);
        Transaccion result4 = adapter.creacion(transaccion, event);
        assertThat(result4.getStatus()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe manejar correctamente montos decimales")
    void testMontosDecimales() {
        // Arrange
        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(123456.78);
        event.setEmail("test@example.com");
        event.setStatus(34);
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(123456.78);
    }

    @Test
    @DisplayName("Debe manejar correctamente emails y nombres largos")
    void testEmailsYNombresLargos() {
        // Arrange
        String emailLargo = "usuario.con.nombre.muy.largo@dominio.empresa.com";
        String nombreLargo = "Juan Carlos Pérez González de la Cruz";

        TransaccionEvent event = new TransaccionEvent();
        event.setId(1L);
        event.setAmount(150000.0);
        event.setEmail(emailLargo);
        event.setFullname(nombreLargo);
        event.setStatus(34);
        event.setOrdenId(100L);

        Transaccion transaccion = new Transaccion();

        // Act
        Transaccion result = adapter.creacion(transaccion, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(emailLargo);
        assertThat(result.getFullName()).isEqualTo(nombreLargo);
    }
}
