package com.arquitectura.promotor;

import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.promotor.consumer.PromotorEventAdaparterImpl;
import com.arquitectura.promotor.entity.Promotor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;
public class PromotorEventAdapterTest {

    @InjectMocks
    private PromotorEventAdaparterImpl adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe convertir correctamente UsuarioEvent a Promotor para creación")
    void testCreacionDesdeUsuarioEvent() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Act
        Promotor result = adapter.creacion(promotor, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getCorreo()).isEqualTo("juan.perez@email.com");
        assertThat(result.getTipoDocumento()).isEqualTo("CC");
        assertThat(result.getCelular()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Promotor existente")
    void testActualizacionPromotorExistente() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez - Actualizado");
        event.setCorreo("juan.perez.updated@email.com");
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        event.setCelular("3007654321");

        Promotor promotorExistente = new Promotor();
        promotorExistente.setNumeroDocumento("12345678");
        promotorExistente.setNombre("Juan Pérez");
        promotorExistente.setCorreo("juan.perez@email.com");
        promotorExistente.setTipoDocumento("CC");
        promotorExistente.setCelular("3001234567");

        // Act
        Promotor result = adapter.creacion(promotorExistente, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getNombre()).isEqualTo("Juan Pérez - Actualizado");
        assertThat(result.getCorreo()).isEqualTo("juan.perez.updated@email.com");
        assertThat(result.getTipoDocumento()).isEqualTo("CC");
        assertThat(result.getCelular()).isEqualTo("3007654321");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre(null);
        event.setCorreo(null);
        event.setTipoDocumentoId(null);
        event.setTipoDocumento(null);
        event.setCelular(null);

        Promotor promotor = new Promotor();

        // Act
        Promotor result = adapter.creacion(promotor, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getNombre()).isNull();
        assertThat(result.getCorreo()).isNull();
        assertThat(result.getTipoDocumento()).isNull();
        assertThat(result.getCelular()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Act
        Promotor result = adapter.creacion(promotor, event);

        // Assert
        assertThat(result).isSameAs(promotor);
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de documento")
    void testDiferentesTiposDocumento() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Test CC
        event.setTipoDocumento("CC");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getTipoDocumento()).isEqualTo("CC");

        // Test CE
        event.setTipoDocumento("CE");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getTipoDocumento()).isEqualTo("CE");

        // Test TI
        event.setTipoDocumento("TI");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getTipoDocumento()).isEqualTo("TI");

        // Test PP
        event.setTipoDocumento("PP");
        Promotor result4 = adapter.creacion(promotor, event);
        assertThat(result4.getTipoDocumento()).isEqualTo("PP");
    }

    @Test
    @DisplayName("Debe manejar diferentes formatos de email")
    void testDiferentesFormatosEmail() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Test email simple
        event.setCorreo("juan@email.com");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getCorreo()).isEqualTo("juan@email.com");

        // Test email con subdominios
        event.setCorreo("juan.perez@empresa.com.co");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getCorreo()).isEqualTo("juan.perez@empresa.com.co");

        // Test email con números
        event.setCorreo("juan123@email123.com");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getCorreo()).isEqualTo("juan123@email123.com");
    }

    @Test
    @DisplayName("Debe manejar diferentes formatos de número de celular")
    void testDiferentesFormatosCelular() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumento("CC");

        Promotor promotor = new Promotor();

        // Test celular estándar
        event.setCelular("3001234567");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getCelular()).isEqualTo("3001234567");

        // Test celular con formato +57
        event.setCelular("+573001234567");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getCelular()).isEqualTo("+573001234567");

        // Test celular con formato 57
        event.setCelular("573001234567");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getCelular()).isEqualTo("573001234567");
    }

    @Test
    @DisplayName("Debe manejar nombres con caracteres especiales")
    void testNombresConCaracteresEspeciales() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Test nombre con tildes
        event.setNombre("José María Pérez");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getNombre()).isEqualTo("José María Pérez");

        // Test nombre con ñ
        event.setNombre("Juan Peña");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getNombre()).isEqualTo("Juan Peña");

        // Test nombre con apostrofe
        event.setNombre("O'Connor");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getNombre()).isEqualTo("O'Connor");
    }

    @Test
    @DisplayName("Debe manejar nombres largos")
    void testNombresLargos() {
        // Arrange
        String nombreLargo = "Juan Carlos Pérez González de la Cruz y Martínez";
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre(nombreLargo);
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Act
        Promotor result = adapter.creacion(promotor, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo(nombreLargo);
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de documento con ID")
    void testTiposDocumentoConId() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Test con tipoDocumentoId = 1 (CC)
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getTipoDocumento()).isEqualTo("CC");

        // Test con tipoDocumentoId = 2 (CE)
        event.setTipoDocumentoId(2L);
        event.setTipoDocumento("CE");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getTipoDocumento()).isEqualTo("CE");

        // Test con tipoDocumentoId = 3 (TI)
        event.setTipoDocumentoId(3L);
        event.setTipoDocumento("TI");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getTipoDocumento()).isEqualTo("TI");
    }

    @Test
    @DisplayName("Debe manejar números de documento como string")
    void testNumeroDocumentoString() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        Promotor promotor = new Promotor();

        // Test número documento normal
        event.setId("12345678");
        Promotor result1 = adapter.creacion(promotor, event);
        assertThat(result1.getNumeroDocumento()).isEqualTo("12345678");

        // Test número documento largo
        event.setId("1234567890");
        Promotor result2 = adapter.creacion(promotor, event);
        assertThat(result2.getNumeroDocumento()).isEqualTo("1234567890");

        // Test número documento con letras (extranjero)
        event.setId("AB123456");
        Promotor result3 = adapter.creacion(promotor, event);
        assertThat(result3.getNumeroDocumento()).isEqualTo("AB123456");
    }
}
