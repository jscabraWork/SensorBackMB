package com.arquitectura.alcancia;

import com.arquitectura.PagosApplication;
import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mockito.ArgumentMatchers;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class AlcanciaServiceTest {

    @Autowired
    private AlcanciaService alcanciaService;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private AlcanciaRepository alcanciaRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private Cliente clienteTest;
    private Tarifa tarifaTest;
    private List<Ticket> ticketsTest;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        clienteTest = new Cliente();
        clienteTest.setNumeroDocumento("12345678");
        clienteTest.setNombre("Juan");
        clienteTest.setCorreo("juan@test.com");
        clienteTest.setCelular("3001234567");
        clienteTest.setTipoDocumento("Cedula");

        tarifaTest = new Tarifa();
        tarifaTest.setId(1L);
        tarifaTest.setPrecio(100.0);
        tarifaTest.setServicio(10.0);
        tarifaTest.setIva(19.0);

        ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setEstado(0); // DISPONIBLE
        ticket1.setNumero("A1");
        ticket1.setTarifa(tarifaTest);

        ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setEstado(0); // DISPONIBLE  
        ticket2.setNumero("A2");
        ticket2.setTarifa(tarifaTest);

        ticketsTest = Arrays.asList(ticket1, ticket2);
    }

    @Test
    @DisplayName("Crear alcancía exitosamente")
    void crearAlcancia_Exitoso() throws Exception {
        // Arrange
        Double precioTotal = 125.0;
        Double precioPagado = 50.0;

        Alcancia alcanciaEsperada = new Alcancia();
        alcanciaEsperada.setId(1L);
        alcanciaEsperada.setCliente(clienteTest);
        alcanciaEsperada.setTickets(ticketsTest);
        alcanciaEsperada.setPrecioTotal(precioTotal);
        alcanciaEsperada.setPrecioParcialPagado(precioPagado);
        alcanciaEsperada.setActiva(true);

        when(alcanciaRepository.save(any(Alcancia.class))).thenReturn(alcanciaEsperada);
        when(ticketService.saveKafka(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        verify(kafkaTemplate).send(any(ProducerRecord.class));


        // Act
        Alcancia resultado = alcanciaService.crear(clienteTest, ticketsTest, precioTotal, tarifaTest);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCliente()).isEqualTo(clienteTest);
        assertThat(resultado.getTickets()).hasSize(2);
        assertThat(resultado.getPrecioTotal()).isEqualTo(precioTotal);
        assertThat(resultado.isActiva()).isTrue();

        verify(alcanciaRepository, atLeastOnce()).save(any(Alcancia.class));
    }

    @Test
    @DisplayName("Crear alcancía con precio pagado negativo - lanza excepción")
    void crearAlcancia_PrecioPagadoNegativo_LanzaExcepcion() {
        // Arrange
        Double precioTotal = 125.0;
        Double precioPagadoNegativo = -50.0;

        // Act & Assert
        assertThatThrownBy(() -> 
            alcanciaService.crear(clienteTest, ticketsTest, precioTotal, tarifaTest)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("El aporte debe ser mayor a 0");
    }

    @Test
    @DisplayName("Aportar a alcancía exitosamente")
    void aportarAlcancia_Exitoso() throws Exception {
        // Arrange
        Alcancia alcancia = new Alcancia();
        alcancia.setId(1L);
        alcancia.setCliente(clienteTest);
        alcancia.setTickets(ticketsTest);
        alcancia.setPrecioTotal(125.0);
        alcancia.setPrecioParcialPagado(25.0);
        alcancia.setActiva(true);

        Double aporte = 50.0;

        when(alcanciaRepository.save(any(Alcancia.class))).thenReturn(alcancia);
        when(ticketService.saveKafka(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        verify(kafkaTemplate).send(any(ProducerRecord.class));

        // Act
        Alcancia resultado = alcanciaService.aportar(alcancia, aporte);

        // Assert
        assertThat(resultado).isNotNull();
        verify(alcanciaRepository, atLeastOnce()).save(any(Alcancia.class));
        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("Aportar cantidad negativa - lanza excepción")
    void aportarAlcancia_CantidadNegativa_LanzaExcepcion() {
        // Arrange
        Alcancia alcancia = new Alcancia();
        alcancia.setActiva(true);
        Double aporteNegativo = -10.0;

        // Act & Assert
        assertThatThrownBy(() -> 
            alcanciaService.aportar(alcancia, aporteNegativo)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("El aporte debe ser mayor a 0");
    }

    @Test
    @DisplayName("Aportar a alcancía inactiva - lanza excepción")
    void aportarAlcancia_AlcanciaInactiva_LanzaExcepcion() {
        // Arrange
        Alcancia alcanciaInactiva = new Alcancia();
        alcanciaInactiva.setActiva(false);
        Double aporte = 50.0;

        // Act & Assert
        assertThatThrownBy(() -> 
            alcanciaService.aportar(alcanciaInactiva, aporte)
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("No se puede aportar a una alcancía inactiva");
    }

    @Test
    @DisplayName("Aportar con suficiente dinero para vender tickets")
    void aportarAlcancia_VendeTickets() throws Exception {
        // Arrange
        // El precio de cada ticket será 100 + 10 + 19 = 129.0 (precio + servicio + iva)
        Double precioTotalTarifa = tarifaTest.getPrecio() + tarifaTest.getServicio() + tarifaTest.getIva(); // 129.0
        
        Alcancia alcancia = new Alcancia();
        alcancia.setId(1L);
        alcancia.setCliente(clienteTest);
        alcancia.setTickets(ticketsTest);
        alcancia.setPrecioTotal(precioTotalTarifa * 2); // 258.0 para 2 tickets
        alcancia.setPrecioParcialPagado(0.0);
        alcancia.setActiva(true);

        Double aporte = precioTotalTarifa + 10.0; // 139.0 - Suficiente para el primer ticket

        when(alcanciaRepository.save(any(Alcancia.class))).thenReturn(alcancia);
        when(ticketService.saveKafka(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        verify(kafkaTemplate).send(any(ProducerRecord.class));

        // Act
        Alcancia resultado = alcanciaService.aportar(alcancia, aporte);

        // Assert
        assertThat(resultado).isNotNull();
        // Verificamos que se intenta vender al menos un ticket ya que tenemos suficiente dinero
        verify(ticketService, atLeastOnce()).saveKafka(any(Ticket.class));
        verify(ticketService, atLeastOnce()).mandarQR(any(Ticket.class));
    }

    @Test
    @DisplayName("SaveKafka guarda alcancía y envía evento")
    void saveKafka_Exitoso() {
        // Arrange
        Alcancia alcancia = new Alcancia();
        alcancia.setId(1L);
        alcancia.setCliente(clienteTest);
        alcancia.setTickets(ticketsTest);
        alcancia.setPrecioParcialPagado(50.0);
        alcancia.setPrecioTotal(125.0);
        alcancia.setActiva(true);

        when(alcanciaRepository.save(any(Alcancia.class))).thenReturn(alcancia);
        when(kafkaTemplate.send(any(ProducerRecord.class)));

        // Act
        Alcancia resultado = alcanciaService.saveKafka(alcancia);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        
        verify(alcanciaRepository).save(alcancia);
        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("DeleteById elimina alcancía y envía evento")
    void deleteById_Exitoso() {
        // Arrange
        Long alcanciaId = 1L;
        Alcancia alcancia = new Alcancia();
        alcancia.setId(alcanciaId);

        when(alcanciaRepository.findById(alcanciaId)).thenReturn(Optional.of(alcancia));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        doNothing().when(alcanciaRepository).deleteById(alcanciaId);

        // Act
        alcanciaService.deleteById(alcanciaId);

        // Assert
        verify(alcanciaRepository).findById(alcanciaId);
        verify(kafkaTemplate).send(any(ProducerRecord.class));
        verify(alcanciaRepository).deleteById(alcanciaId);
    }

    @Test
    @DisplayName("DeleteById con ID inexistente - lanza excepción")
    void deleteById_IdInexistente_LanzaExcepcion() {
        // Arrange
        Long alcanciaIdInexistente = 999L;
        when(alcanciaRepository.findById(alcanciaIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            alcanciaService.deleteById(alcanciaIdInexistente)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("No se encontró ninguna alcancía con el id proporcionado");

        verify(alcanciaRepository).findById(alcanciaIdInexistente);
        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
        verify(alcanciaRepository, never()).deleteById(anyLong());
    }
}