package com.arquitectura.ticket;

import com.arquitectura.PagosApplication;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TicketTestRepository {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private TicketRepository ticketRepository;


    private Ticket ticket;
    private Orden orden;
    private Localidad localidad;

    @BeforeEach
    void setup() {
        // Crear una Localidad válida
        localidad = Localidad.builder()
                .nombre("VIP")
                .tipo(0)
                .aporteMinimo(0.0)
                .build();

        testEntityManager.persist(localidad);

        // Crear el Ticket asociado a la Localidad
        ticket = Ticket.builder()
                .tipo(0)
                .estado(3)
                .numero("TCKT-001")
                .localidad(localidad) // Asignar localidad válida
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        testEntityManager.persist(ticket);
        testEntityManager.flush();

        // Crear la Orden
        orden = Orden.builder()
                .valorOrden(1000.0)
                .estado(1)
                .tipo(0)
                .tickets(new ArrayList<>()) // aún no asociamos el ticket
                .cliente(null)
                .transacciones(new ArrayList<>())
                .build();

        testEntityManager.persist(orden);
        testEntityManager.flush();
    }



    @Test
    @DisplayName("Buscar ticket por ID existente")
    void testFindById() {
        Long ticketId = ticket.getId();
        Ticket encontrada = ticketRepository.findById(ticketId).orElse(null);
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getId()).isEqualTo(ticketId);
    }

    @Test
    @DisplayName("Buscar ticket por ID no existente")
    void testFindByIdNotFound() {
        Ticket noEncontrado = ticketRepository.findById(999L).orElse(null);
        assertThat(noEncontrado).isNull();
    }



    @Test
    @DisplayName("trae exitosamente todos los tickets por orden id")
    void GetsAllTicketsExitoso(){
        Long ordenIdInexistente = 999L;
        List<Ticket> tickets = ticketRepository.findByOrdenesId(ordenIdInexistente);
        assertThat(tickets).isEmpty();
    }

    @Test
    @DisplayName("Buscar tickets por ordenId - orden con tickets asociados")
    void testFindByOrdenIdConTickets() {
        // Asociar el ticket a la orden
        orden.getTickets().add(ticket);
        ticket.getOrdenes().add(orden);
        testEntityManager.persist(orden);
        testEntityManager.persist(ticket);
        testEntityManager.flush();
        
        List<Ticket> tickets = ticketRepository.findByOrdenesId(orden.getId());
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getId()).isEqualTo(ticket.getId());
    }

    @Test
    @DisplayName("Buscar tickets palco padres por localidad y estado")
    void testFindTicketsCompletosYPalcosPadresByLocalidadAndEstado() {
        Localidad localidad = Localidad.builder()
                .nombre("VIP")
                .tipo(0)
                .aporteMinimo(0.0)
                .build();
        testEntityManager.persist(localidad);

        Ticket ticketPalcoPadre = Ticket.builder()
                .tipo(0)
                .estado(1)
                .localidad(localidad)
                .palco(null)
                .build();

        testEntityManager.persist(ticketPalcoPadre);
        testEntityManager.flush();

        Pageable pageable = Pageable.ofSize(10);
        var result = ticketRepository.findTicketsCompletosYPalcosPadresByLocalidadAndEstado(localidad.getId(), 1, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getLocalidad().getId()).isEqualTo(localidad.getId());
    }


    @Test
    @DisplayName("Buscar tickets por ID de palco")
    void testFindByPalcoId() {
        Ticket palco = Ticket.builder().build();
        testEntityManager.persist(palco);

        Ticket hijo = Ticket.builder().palco(palco).build();
        testEntityManager.persist(hijo);

        List<Ticket> result = ticketRepository.findByPalcoId(palco.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPalco().getId()).isEqualTo(palco.getId());
    }

    @Test
    @DisplayName("Contar asientos por palco")
    void testContarAsientosPorPalco() {
        Ticket palco = Ticket.builder().build();
        testEntityManager.persist(palco);

        Ticket hijo1 = Ticket.builder().palco(palco).build();
        Ticket hijo2 = Ticket.builder().palco(palco).build();
        testEntityManager.persist(hijo1);
        testEntityManager.persist(hijo2);

        List<Object[]> result = ticketRepository.contarAsientosPorPalco(List.of(palco.getId()));
        assertThat(result).isNotEmpty();
        assertThat(((Long) result.get(0)[1])).isEqualTo(3); // 2 hijos + 1 padre
    }


    @Test
    @DisplayName("Buscar ticket completo y palco padre por localidad y estado")
    void testFindTicketCompletoYPalcoPadreByLocalidadAndEstado() {
        // Crear una Localidad
        Localidad localidad = Localidad.builder()
                .nombre("Palcos")
                .tipo(1)
                .aporteMinimo(0.0)
                .build();
        testEntityManager.persist(localidad);

        // Crear un Ticket tipo 0 (para que coincida con la query)
        Ticket palcoPadre = Ticket.builder()
                .tipo(0)  // ← Cambiar a tipo 0
                .estado(2)
                .localidad(localidad)
                .palco(null)
                .build();
        testEntityManager.persist(palcoPadre);
        testEntityManager.flush();

        // Ejecutar la consulta
        Optional<Ticket> result = ticketRepository.findTicketCompletoYPalcoPadreByLocalidadAndEstado(
                palcoPadre.getId(),
                localidad.getId(),
                2
        );

        // Verificar que se encuentra
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(palcoPadre.getId());
    }

    @Test
    @DisplayName("Buscar tickets por localidad id- exitosamente")
    void testFindTicketByLocalidadId() {
        Long localidadId = localidad.getId();
        List<Ticket> tickets = ticketRepository.findByLocalidadId(localidadId);
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getLocalidad().getId()).isEqualTo(localidadId);
    }

    @Test
    @DisplayName("Falla Buscar tickets por localidad id- localidad inexistente")
    void testFindTicketByLocalidadIdNoEncontrado() {
        Long localidadId = 999999L;
        List<Ticket> tickets = ticketRepository.findByLocalidadId(localidadId);
        assertThat(tickets).isEmpty();
    }

    @Test
    @DisplayName("Buscar tickets por localidad y estado con límite")
    void testFindByLocalidadIdAndEstadoLimitedTo() {
        // Crear tickets adicionales con diferentes estados
        Ticket ticket1 = Ticket.builder()
                .tipo(0)
                .estado(0) // DISPONIBLE
                .numero("TCKT-002")
                .localidad(localidad)
                .palco(null)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();
                
        Ticket ticket2 = Ticket.builder()
                .tipo(0)
                .estado(1) // VENDIDO
                .numero("TCKT-003")
                .localidad(localidad)
                .palco(null)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        testEntityManager.persist(ticket1);
        testEntityManager.persist(ticket2);
        testEntityManager.flush();

        Pageable pageable = Pageable.ofSize(5);
        List<Ticket> tickets = ticketRepository.findByLocalidadIdAndEstadoLimitedTo(localidad.getId(), 0, pageable);
        
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getEstado()).isEqualTo(0);
        assertThat(tickets.get(0).getPalco()).isNull();
    }

    @Test
    @DisplayName("Buscar tickets padres por localidad (sin palco)")
    void testFindByLocalidadIdAndPalcoIdIsNull() {
        // Crear un ticket padre
        Ticket ticketPadre = Ticket.builder()
                .tipo(1)
                .estado(0)
                .numero("PADRE-001")
                .localidad(localidad)
                .palco(null)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        // Crear un ticket hijo
        Ticket ticketHijo = Ticket.builder()
                .tipo(0)
                .estado(0)
                .numero("HIJO-001")
                .localidad(localidad)
                .palco(ticketPadre)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        testEntityManager.persist(ticketPadre);
        testEntityManager.persist(ticketHijo);
        testEntityManager.flush();

        List<Ticket> ticketsPadres = ticketRepository.findByLocalidadIdAndPalcoIdIsNull(localidad.getId());
        
        assertThat(ticketsPadres).hasSize(2); // ticket original + ticketPadre
        assertThat(ticketsPadres).allMatch(t -> t.getPalco() == null);
    }

    @Test
    @DisplayName("Contar asientos disponibles por palcos")
    void testContarAsientosDisponiblesPorPalcos() {
        // Crear palco con asientos disponibles y no disponibles
        Ticket palco = Ticket.builder()
                .estado(0) // DISPONIBLE
                .tipo(1)
                .numero("PALCO-001")
                .localidad(localidad)
                .palco(null)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();
        testEntityManager.persist(palco);

        // Asiento disponible
        Ticket asientoDisponible = Ticket.builder()
                .estado(0) // DISPONIBLE
                .tipo(0)
                .numero("ASIENTO-001")
                .localidad(localidad)
                .palco(palco)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        // Asiento vendido
        Ticket asientoVendido = Ticket.builder()
                .estado(1) // VENDIDO
                .tipo(0)
                .numero("ASIENTO-002")
                .localidad(localidad)
                .palco(palco)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();

        testEntityManager.persist(asientoDisponible);
        testEntityManager.persist(asientoVendido);
        testEntityManager.flush();

        List<Object[]> resultado = ticketRepository.contarAsientosDisponiblesPorPalcos(List.of(palco.getId()));
        
        assertThat(resultado).isNotEmpty();
        Object[] conteo = resultado.get(0);
        Long palcoId = (Long) conteo[0];
        Long asientosDisponibles = (Long) conteo[1];
        
        assertThat(palcoId).isEqualTo(palco.getId());
        assertThat(asientosDisponibles).isEqualTo(2L); // palco + 1 asiento disponible
    }

    @Test
    @DisplayName("Buscar ID de ticket y localidad por IDs de tickets")
    void testFindTicketIdAndLocalidadIdByTicketIds() {
        // Crear ticket adicional en otra localidad
        Localidad otraLocalidad = Localidad.builder()
                .nombre("General")
                .tipo(0)
                .aporteMinimo(0.0)
                .build();
        testEntityManager.persist(otraLocalidad);

        Ticket otroTicket = Ticket.builder()
                .tipo(0)
                .estado(0)
                .numero("TCKT-OTRO")
                .localidad(otraLocalidad)
                .servicios(new ArrayList<>())
                .asientos(new ArrayList<>())
                .ordenes(new ArrayList<>())
                .build();
        testEntityManager.persist(otroTicket);
        testEntityManager.flush();

        List<Long> ticketIds = List.of(ticket.getId(), otroTicket.getId());
        List<Object[]> resultado = ticketRepository.findTicketIdAndLocalidadIdByTicketIds(ticketIds);
        
        assertThat(resultado).hasSize(2);
        
        // Verificar primer resultado
        Object[] primer = resultado.get(0);
        Long ticketId1 = (Long) primer[0];
        Long localidadId1 = (Long) primer[1];
        
        assertThat(ticketId1).isIn(ticket.getId(), otroTicket.getId());
        assertThat(localidadId1).isIn(localidad.getId(), otraLocalidad.getId());
    }

}
