package com.arquitectura.orden.helper;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.service.TarifaService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrdenCreationHelper {

    private static final Logger logger = LoggerFactory.getLogger(OrdenCreationHelper.class);

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TarifaService tarifaService;

    /**
     * Interfaz funcional para crear instancias de orden
     */
    @FunctionalInterface
    public interface OrdenFactory<T extends Orden> {
        T create(Evento evento, Cliente cliente, List<Ticket> tickets);
    }

    /**
     * Interfaz funcional para crear instancias de orden con tarifa específica
     */
    @FunctionalInterface
    public interface OrdenFactoryConTarifa<T extends Orden> {
        T create(Evento evento, Cliente cliente, List<Ticket> tickets, Tarifa tarifa);
    }

    /**
     * Crea una orden no numerada (localidad general)
     */
    @Transactional("transactionManager")
    public <T extends Orden> T crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                                     Long pLocalidadId, OrdenFactory<T> factory) throws Exception {

        //-----VALIDACIONES DE ENTRADA------
        //1. Encontrar la localidad
        Localidad localidad = localidadService.findById(pLocalidadId);
        if (localidad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada");
        }

        //2. Encontrar el cliente
        Cliente cliente = clienteService.findByNumeroDocumento(pNumeroDocumento);
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        //3. Encontrar el evento
        Evento evento = eventoService.findById(pEventoId);
        if (evento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        //4. Encontrar la cantidad de tickets necesarios para la orden y validar que existan suficientes tickets disponibles
        List<Ticket> tickets = ticketService.findTicketsByLocalidadIdAndEstado(pLocalidadId, 0, pCantidad);
        if (tickets.size() < pCantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No hay suficientes tickets disponibles en la localidad seleccionada, reduce la cantidad o contacta a servicio al cliente");
        }
        //----FIN VALIDACIONES------

        //Este constructor crea la orden, calcula los datos necesarios y asigna los tickets
        T orden = factory.create(evento, cliente, tickets);

        //Guardar estado de todos los tickets, no se publica en kafka porque no se ha confirmado la orden
        //ticketService.saveAll(orden.getTickets());

        return orden;
    }

    /**
     * Crea una orden numerada (asientos específicos)
     */
    @Transactional("transactionManager")
    public <T extends Orden> T crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento,
                                                   OrdenFactory<T> factory) throws Exception {
        try {
            //-----VALIDACIONES DE ENTRADA------

            //Encontrar el cliente
            Cliente cliente = clienteService.findByNumeroDocumento(pNumeroDocumento);
            if (cliente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
            }

            //Encontrar el evento
            Evento evento = eventoService.findById(pEventoId);
            if (evento == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
            }

            //Recuperar todos los tickets de la base de datos para evitar inconsistencias
            List<Ticket> ticketsBD = ticketService.findAllById(tickets.stream().map(Ticket::getId).toList());

            //Validar que todos los tickets pertenezcan a la misma localidad
            boolean mismaLocalidad = ticketsBD.stream().allMatch(t -> t.getLocalidad().equals(ticketsBD.get(0).getLocalidad()));
            if (!mismaLocalidad) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los tickets deben pertenecer a la misma localidad");
            }

            //Validar que todos los tickets estén en proceso (estado 3)
            boolean todosDisponibles = ticketsBD.stream().allMatch(t -> t.getEstado() == 3);
            if (!todosDisponibles) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ocurrió un error, refresca la página y vuelve a intentarlo");
            }

            //-----FIN VALIDACIONES------

            //Este constructor crea la orden, calcula los datos necesarios y asigna los tickets
            T orden = factory.create(evento, cliente, ticketsBD);

            //Guardar estado de todos los tickets, no se publica en kafka porque no se ha confirmado la orden
            //ticketService.saveAll(orden.getTickets());

            return orden;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear la orden", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la orden");
        }
    }

    /**
     * Crea una orden para un palco individual a partir del id del padre
     */
    @Transactional("transactionManager")
    public <T extends Orden> T crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId,
                                                          String pNumeroDocumento, OrdenFactory<T> factory) throws Exception {
        try {
            // Validaciones de entrada
            Cliente cliente = clienteService.findByNumeroDocumento(pNumeroDocumento);
            if (cliente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
            }

            Evento evento = eventoService.findById(pEventoId);
            if (evento == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
            }

            Ticket ticketPadre = ticketService.findById(pTicketPadreId);
            if (ticketPadre == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket padre no encontrado");
            }

            // Lógica de selección de tickets corregida
            List<Ticket> ticketsHijosDisponibles = ticketPadre.getAsientos().stream()
                    .filter(Ticket::isDisponible)
                    .toList();

            List<Ticket> ticketsSeleccionados = new ArrayList<>();

            // Calcular capacidad total disponible (hijos + padre si está disponible)
            int capacidadTotal = ticketsHijosDisponibles.size();
            if (ticketPadre.isDisponible()) {
                capacidadTotal += 1; // El padre cuenta como 1 ticket adicional
            }

            // Verificar si hay suficiente capacidad total
            if (capacidadTotal < pCantidad) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay suficientes tickets disponibles para completar la orden");
            }

            // Estrategia de selección: usar hijos primero, padre solo si es necesario
            if (ticketsHijosDisponibles.size() >= pCantidad) {
                // Caso 1: Suficientes hijos disponibles - usar solo hijos
                ticketsSeleccionados = ticketsHijosDisponibles.stream()
                        .limit(pCantidad)
                        .collect(Collectors.toList());
            } else {
                // Caso 2: No hay suficientes hijos - usar todos los hijos + padre
                ticketsSeleccionados.addAll(ticketsHijosDisponibles);

                // Verificar que el padre esté disponible (ya validamos capacidad arriba)
                if (ticketPadre.isDisponible()) {
                    ticketsSeleccionados.add(ticketPadre);
                }
            }

            // Crear la orden y guardar
            T orden = factory.create(evento, cliente, ticketsSeleccionados);

            // Guardar el estado de los tickets seleccionados
            // ticketService.saveAll(orden.getTickets());

            return orden;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear la orden para palco individual", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al crear la orden para palco individual: " + e.getMessage());
        }
    }

    /**
     * Crea una orden no numerada con una tarifa específica
     */
    @Transactional("transactionManager")
    public <T extends Orden> T crearOrdenNoNumeradaConTarifa(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                                              Long pLocalidadId, Long pTarifaId, OrdenFactoryConTarifa<T> factory) throws Exception {

        //-----VALIDACIONES DE ENTRADA------
        //1. Encontrar la localidad
        Localidad localidad = localidadService.findById(pLocalidadId);
        if (localidad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada");
        }

        //2. Encontrar el cliente
        Cliente cliente = clienteService.findByNumeroDocumento(pNumeroDocumento);
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        //3. Encontrar el evento
        Evento evento = eventoService.findById(pEventoId);
        if (evento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        //4. Encontrar la tarifa específica
        Tarifa tarifa = tarifaService.findById(pTarifaId);
        if (tarifa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada");
        }

        //5. Encontrar la cantidad de tickets necesarios para la orden y validar que existan suficientes tickets disponibles
        List<Ticket> tickets = ticketService.findTicketsByLocalidadIdAndEstado(pLocalidadId, 0, pCantidad);
        if (tickets.size() < pCantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No hay suficientes tickets disponibles en la localidad seleccionada, reduce la cantidad o contacta a servicio al cliente");
        }
        //----FIN VALIDACIONES------

        //Este constructor crea la orden con la tarifa específica, calcula los datos necesarios y asigna los tickets
        T orden = factory.create(evento, cliente, tickets, tarifa);

        //Guardar estado de todos los tickets, no se publica en kafka porque no se ha confirmado la orden
        //ticketService.saveAll(orden.getTickets());

        return orden;
    }
}
