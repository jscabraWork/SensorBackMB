package com.arquitectura.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.tipo_documento.TipoDocumento;
import com.arquitectura.tipo_documento.TipoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class UsuarioEventAdapterTest {

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private UsuarioEventAdaparterImpl adapter;

    private TipoDocumento tipoDocumentoCC;
    private TipoDocumento tipoDocumentoCE;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear entidades TipoDocumento para los tests
        tipoDocumentoCC = new TipoDocumento();
        tipoDocumentoCC.setId(1L);
        tipoDocumentoCC.setNombre("Cedula");

        tipoDocumentoCE = new TipoDocumento();
        tipoDocumentoCE.setId(2L);
        tipoDocumentoCE.setNombre("Cedula de Extranjeria");

        // Configurar comportamiento del mock
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(tipoDocumentoCC));
        when(tipoDocumentoRepository.findById(2L)).thenReturn(Optional.of(tipoDocumentoCE));
    }

    @Test
    @DisplayName("Debe convertir correctamente UsuarioEvent a Cliente para creación")
    void testCreacionDesdeUsuarioEvent() {
        UsuarioEvent event = new UsuarioEvent();
        event.setId("123456");
        event.setNombre("Juan Pérez");
        event.setCorreo("juan.perez@example.com");
        event.setTipoDocumentoId(1L);
        event.setCelular("3001234567");

        Cliente cliente = new Cliente();
        Cliente result = adapter.creacion(cliente, event);

        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("123456");
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getCorreo()).isEqualTo("juan.perez@example.com");
        assertThat(result.getTipoDocumento()).isEqualTo(tipoDocumentoCC);
        assertThat(result.getCelular()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("Debe actualizar correctamente un Cliente existente")
    void testActualizacionClienteExistente() {
        UsuarioEvent event = new UsuarioEvent();
        event.setId("123456");
        event.setNombre("Juan Pérez Actualizado");
        event.setCorreo("juan.actualizado@example.com");
        event.setTipoDocumentoId(2L);
        event.setCelular("3009876543");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setNumeroDocumento("123456");
        clienteExistente.setNombre("Juan Pérez");
        clienteExistente.setCorreo("juan.perez@example.com");
        clienteExistente.setTipoDocumento(tipoDocumentoCC);
        clienteExistente.setCelular("3001234567");

        Cliente result = adapter.creacion(clienteExistente, event);

        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("123456");
        assertThat(result.getNombre()).isEqualTo("Juan Pérez Actualizado");
        assertThat(result.getCorreo()).isEqualTo("juan.actualizado@example.com");
        assertThat(result.getTipoDocumento()).isEqualTo(tipoDocumentoCE);
        assertThat(result.getCelular()).isEqualTo("3009876543");
    }

    @Test
    @DisplayName("Debe manejar valores nulos en el evento")
    void testManejoValoresNulos() {
        UsuarioEvent event = new UsuarioEvent();
        event.setId("123456");
        event.setNombre(null);
        event.setCorreo(null);
        event.setTipoDocumentoId(null);
        event.setCelular(null);

        Cliente cliente = new Cliente();
        Cliente result = adapter.creacion(cliente, event);

        assertThat(result).isNotNull();
        assertThat(result.getNumeroDocumento()).isEqualTo("123456");
        assertThat(result.getNombre()).isNull();
        assertThat(result.getCorreo()).isNull();
        assertThat(result.getTipoDocumento()).isNull();
        assertThat(result.getCelular()).isNull();
    }

    @Test
    @DisplayName("Debe preservar la referencia del mismo objeto")
    void testPreservaReferenciaObjeto() {
        UsuarioEvent event = new UsuarioEvent();
        event.setId("123456");
        event.setNombre("Juan Pérez");
        event.setTipoDocumentoId(1L);

        Cliente cliente = new Cliente();
        Cliente result = adapter.creacion(cliente, event);
        assertThat(result).isSameAs(cliente);
    }

    @Test
    @DisplayName("Debe manejar caracteres especiales en los campos")
    void testManejoCaracteresEspeciales() {
        UsuarioEvent event = new UsuarioEvent();
        event.setId("123456");
        event.setNombre("Juán Pérez González");
        event.setCorreo("juan.perez+test@example.com");
        event.setTipoDocumentoId(1L);
        event.setCelular("+57 300 123-4567");

        Cliente cliente = new Cliente();
        Cliente result = adapter.creacion(cliente, event);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Juán Pérez González");
        assertThat(result.getCorreo()).isEqualTo("juan.perez+test@example.com");
        assertThat(result.getCelular()).isEqualTo("+57 300 123-4567");
    }
}
