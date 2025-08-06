package com.arquitectura.puntofisico;

import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.puntofisico.consumer.PuntoFisicoEventAdaparterImpl;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;
public class PuntoFisicoEventAdapterTest {

    @InjectMocks
    private PuntoFisicoEventAdaparterImpl adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe convertir correctamente UsuarioEvent a PuntoFisico para creación")
    void testCreacionDesdeUsuarioEvent() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@email.com");
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        event.setCelular("3001234567");

        PuntoFisico puntofisico = new PuntoFisico();

        // Act
        PuntoFisico result = adapter.creacion(puntofisico, event);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("12345678");
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getCorreo()).isEqualTo("juan.perez@email.com");
        assertThat(result.getTipoDocumento()).isEqualTo("CC");
        assertThat(result.getCelular()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("Debe actualizar correctamente un PuntoFisico existente")
    void testActualizacionPuntoFisicoExistente() {
        // Arrange
        UsuarioEvent event = new UsuarioEvent();
        event.setId("12345678");
        event.setNombre("Juan Pérez - Actualizado");
        event.setCorreo("juan.perez.updated@email.com");
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        event.setCelular("3007654321");

        PuntoFisico puntofisicoExistente = new PuntoFisico();
        puntofisicoExistente.setNumeroDocumento("12345678");
        puntofisicoExistente.setNombre("Juan Pérez");
        puntofisicoExistente.setCorreo("juan.perez@email.com");
        puntofisicoExistente.setTipoDocumento("CC");
        puntofisicoExistente.setCelular("3001234567");

        // Act
        PuntoFisico result = adapter.creacion(puntofisicoExistente, event);

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

        PuntoFisico puntofisico = new PuntoFisico();

        // Act
        PuntoFisico result = adapter.creacion(puntofisico, event);

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

        PuntoFisico puntofisico = new PuntoFisico();

        // Act
        PuntoFisico result = adapter.creacion(puntofisico, event);

        // Assert
        assertThat(result).isSameAs(puntofisico);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test CC
        event.setTipoDocumento("CC");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getTipoDocumento()).isEqualTo("CC");

        // Test CE
        event.setTipoDocumento("CE");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getTipoDocumento()).isEqualTo("CE");

        // Test TI
        event.setTipoDocumento("TI");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
        assertThat(result3.getTipoDocumento()).isEqualTo("TI");

        // Test PP
        event.setTipoDocumento("PP");
        PuntoFisico result4 = adapter.creacion(puntofisico, event);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test email simple
        event.setCorreo("juan@email.com");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getCorreo()).isEqualTo("juan@email.com");

        // Test email con subdominios
        event.setCorreo("juan.perez@empresa.com.co");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getCorreo()).isEqualTo("juan.perez@empresa.com.co");

        // Test email con números
        event.setCorreo("juan123@email123.com");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test celular estándar
        event.setCelular("3001234567");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getCelular()).isEqualTo("3001234567");

        // Test celular con formato +57
        event.setCelular("+573001234567");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getCelular()).isEqualTo("+573001234567");

        // Test celular con formato 57
        event.setCelular("573001234567");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test nombre con tildes
        event.setNombre("José María Pérez");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getNombre()).isEqualTo("José María Pérez");

        // Test nombre con ñ
        event.setNombre("Juan Peña");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getNombre()).isEqualTo("Juan Peña");

        // Test nombre con apostrofe
        event.setNombre("O'Connor");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Act
        PuntoFisico result = adapter.creacion(puntofisico, event);

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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test con tipoDocumentoId = 1 (CC)
        event.setTipoDocumentoId(1L);
        event.setTipoDocumento("CC");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getTipoDocumento()).isEqualTo("CC");

        // Test con tipoDocumentoId = 2 (CE)
        event.setTipoDocumentoId(2L);
        event.setTipoDocumento("CE");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getTipoDocumento()).isEqualTo("CE");

        // Test con tipoDocumentoId = 3 (TI)
        event.setTipoDocumentoId(3L);
        event.setTipoDocumento("TI");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
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

        PuntoFisico puntofisico = new PuntoFisico();

        // Test número documento normal
        event.setId("12345678");
        PuntoFisico result1 = adapter.creacion(puntofisico, event);
        assertThat(result1.getNumeroDocumento()).isEqualTo("12345678");

        // Test número documento largo
        event.setId("1234567890");
        PuntoFisico result2 = adapter.creacion(puntofisico, event);
        assertThat(result2.getNumeroDocumento()).isEqualTo("1234567890");

        // Test número documento con letras (extranjero)
        event.setId("AB123456");
        PuntoFisico result3 = adapter.creacion(puntofisico, event);
        assertThat(result3.getNumeroDocumento()).isEqualTo("AB123456");
    }
}
