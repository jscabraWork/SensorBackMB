package com.arquitectura.cliente;

import com.arquitectura.cliente.consumer.ClienteEventAdapter;
import com.arquitectura.cliente.consumer.ClienteServiceConsumerImpl;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.tipo_documento.TipoDocumento;
import com.arquitectura.tipo_documento.TipoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class ClienteServiceConsumerTest {

    @Mock private ClienteRepository repository;
    @Mock private MessageService messageService;
    @Mock private ClienteEventAdapter adapter;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private ClienteServiceConsumerImpl clienteServiceConsumer;

    private TipoDocumento tipoDocumentoCC;
    private String messageId = "test-message-id";
    private String messageKey = "test-key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear la entidad TipoDocumento para los tests
        tipoDocumentoCC = new TipoDocumento();
        tipoDocumentoCC.setId(1L);
        tipoDocumentoCC.setNombre("Cedula");

        // Configurar comportamiento del repositorio de tipos de documento
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(tipoDocumentoCC));
    }

    @Test
    @DisplayName("Debe procesar evento de creación de cliente correctamente")
    void testHandleCreateEvent_newClient() {
        // Configuración
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("123456");
        evt.setNombre("Juan Pérez");
        evt.setCorreo("juan.perez@example.com");
        evt.setTipoDocumentoId(1L);
        evt.setCelular("3001234567");

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNumeroDocumento(evt.getId());
        nuevoCliente.setNombre(evt.getNombre());
        nuevoCliente.setTipoDocumento(tipoDocumentoCC);

        when(messageService.existeMessage(messageId)).thenReturn(false);
        when(repository.findById(evt.getId())).thenReturn(Optional.empty());
        when(adapter.creacion(any(Cliente.class), eq(evt))).thenReturn(nuevoCliente);

        // Ejecución
        clienteServiceConsumer.handleCreateEvent(evt, messageId, messageKey);

        // Verificación
        verify(repository).save(any(Cliente.class));
        verify(messageService).crearMensaje(eq(messageId), eq(evt.getId()));
    }

    @Test
    @DisplayName("Debe procesar evento de actualización de cliente existente")
    void testHandleCreateEvent_existingClient() {
        // Configuración
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("123456");
        evt.setNombre("Juan Pérez Actualizado");
        evt.setTipoDocumentoId(1L);

        Cliente clienteExistente = new Cliente();
        clienteExistente.setNumeroDocumento(evt.getId());
        clienteExistente.setTipoDocumento(tipoDocumentoCC);

        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setNumeroDocumento(evt.getId());
        clienteActualizado.setNombre(evt.getNombre());
        clienteActualizado.setTipoDocumento(tipoDocumentoCC);

        when(messageService.existeMessage(messageId)).thenReturn(false);
        when(repository.findById(evt.getId())).thenReturn(Optional.of(clienteExistente));
        when(adapter.creacion(eq(clienteExistente), eq(evt))).thenReturn(clienteActualizado);

        // Ejecución
        clienteServiceConsumer.handleCreateEvent(evt, messageId, messageKey);

        // Verificación
        verify(repository).save(any(Cliente.class));
        verify(messageService).crearMensaje(eq(messageId), eq(evt.getId()));
    }

    @Test
    @DisplayName("Debe evitar procesar mensaje duplicado")
    void testHandleCreateEvent_duplicateMessage() {
        // Configuración
        UsuarioEvent evt = new UsuarioEvent();
        evt.setId("123456");
        evt.setTipoDocumentoId(1L);

        when(messageService.existeMessage(messageId)).thenReturn(true);

        // Ejecución
        clienteServiceConsumer.handleCreateEvent(evt, messageId, messageKey);

        // Verificación
        verify(repository, never()).save(any(Cliente.class));
        verify(messageService, never()).crearMensaje(anyString(), anyString());
        verify(adapter, never()).creacion(any(), any());
    }


}
