package com.arquitectura.ticket.service;

import com.arquitectura.aws.AWSS3Service;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.service.ClienteService;
import com.arquitectura.clients.ReporteFeignClient;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.dto.MisTicketsDto;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.localidad.service.LocalidadService;
import com.arquitectura.pdf.PdfService;
import com.arquitectura.seguro.service.SeguroService;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import com.arquitectura.events.TicketEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl extends CommonServiceImpl<Ticket, TicketRepository> implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private static final String QR_CODE_IMAGE_PATH = "./uploads/";


    @Autowired
    private TicketFactory ticketFactory;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private SeguroService seguroService;

    @Autowired
    private ConfigSeguroService configService;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private AWSS3Service awsService;

    @Autowired
    private PdfService servicioPDF;

    @Autowired
    private SendEmailAmazonService emailService;

    @Autowired
    private EncriptarTexto encriptador;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ReporteFeignClient reporteFeignClient;

    @Value("${tickets.topic}")
    private String ticketsTopic;

    /**
     * trae todos los tickets de una orden por su id
     *
     * @param ordenId El ID del ticket a eliminar
     */
    @Override
    public List<Ticket> getAllByOrdenId(Long ordenId) {
        return repository.findByOrdenesId(ordenId);
    }

    @Override
    public Ticket getByLocalidadAndEstado(Long pId, Long localidadId, int estado) {
        Optional<Ticket> ticketOpt = repository.findTicketCompletoYPalcoPadreByLocalidadAndEstado(pId, localidadId, estado);

        if (ticketOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el ticket con el ID proporcionado");
        }

        Ticket ticket = ticketOpt.get();

        // Solo los palcos padres tienen conteo de personas
        if (ticket.getTipo() == 1) {
            List<Long> idsPalcos = List.of(ticket.getId());
            List<Object[]> conteo = repository.contarAsientosPorPalco(idsPalcos);

            if (!conteo.isEmpty()) {
                Object[] fila = conteo.get(0);
                ticket.setPersonasPorTicket(((Long) fila[1]).intValue());
            } else {
                ticket.setPersonasPorTicket(1);
            }
        } else {
            ticket.setPersonasPorTicket(1);
        }

        return ticket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Ticket> getAllByLocalidadIdAndEstado(Long localidadId, int estado, int page, int size) {
        Sort sort;

        if (estado == 0) {
            sort = Sort.by(Sort.Direction.ASC, "creationDate");
        } else if (estado == 1 || estado == 2 || estado == 3) {
            sort = Sort.by(Sort.Direction.ASC, "lastModifiedDate");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Ticket> ticketsPage = repository.findTicketsCompletosYPalcosPadresByLocalidadAndEstado(localidadId, estado, pageable);
        List<Ticket> tickets = ticketsPage.getContent();

        // Si no hay tickets, retornamos la página vacía
        if (tickets.isEmpty()) {
            return ticketsPage;
        }

        // Obtenemos los IDs de todos los tickets
        List<Long> idsTickets = tickets.stream().map(Ticket::getId).toList();

        // Contamos los asientos/hijos para cada ticket
        List<Object[]> conteo = repository.contarAsientosPorPalco(idsTickets);
        Map<Long, Integer> personasPorTicket = new HashMap<>();
        for (Object[] fila : conteo) {
            personasPorTicket.put((Long) fila[0], ((Long) fila[1]).intValue());
        }

        // Cargamos los ingresos con sus días para todos los tickets
        List<Ingreso> ingresos = ingresoRepository.findByTicketIdInWithDia(idsTickets);

        // Agrupamos los ingresos por ticket ID
        Map<Long, List<Ingreso>> ingresosPorTicket = ingresos.stream()
                .collect(Collectors.groupingBy(i -> i.getTicket().getId()));

        // Asignamos el conteo y los ingresos a cada ticket
        for (Ticket ticket : tickets) {
            // Asignar personas por ticket
            ticket.setPersonasPorTicket(personasPorTicket.getOrDefault(ticket.getId(), 1));

            // Asignar ingresos al ticket
            List<Ingreso> ticketIngresos = ingresosPorTicket.getOrDefault(ticket.getId(), new ArrayList<>());
            ticket.setIngresos(ticketIngresos);
        }

        return ticketsPage;
    }

    /**
     * {@inheritDoc}
     */
    public List<Ticket> obtenerHijosDelPalco(Long idPalco) {
        return repository.findByPalcoId(idPalco);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public List<Ticket> crearTickets(Long localidadId,
                                     Integer numeroArriba,
                                     Integer numeroAbajo,
                                     String letra,
                                     boolean numerado,
                                     int personas) {

        Localidad localidad = localidadRepository.findByIdWithDias(localidadId)
                .orElseThrow(() -> new RuntimeException("Localidad no encontrada"));

        List<Ticket> ticketsCreados = new ArrayList<>();

        for (int i = numeroAbajo; i <= numeroArriba; i++) {

            String numero = calcularNumero(i, letra, numerado);

            // Configurar factory para este ticket
            ticketFactory.setLocalidad(localidad);
            ticketFactory.setNumeroTicket(numero);
            ticketFactory.setTipo(0);
            ticketFactory.setEstado(0);

            // Crear ticket principal
            Ticket ticket = ticketFactory.crear();

            // Crear asientos si es necesario
            if (personas > 1) {
                List<Ticket> asientos = ticketFactory.setAsientos(personas, ticket);
                ticket.setAsientos(asientos);
            }

            ticketsCreados.add(ticket);
        }

        // Guardar todo con cascading automático
        saveAll(ticketsCreados);

        logger.info("Tickets creados. Total: {}, LocalidadId: {}", ticketsCreados.size(), localidadId);

        crearTicketsReporte(ticketsCreados, localidadId);

        logger.info("Reporte enviado exitosamente para localidad: {}", localidadId);

        return ticketsCreados;
    }

    private String calcularNumero(int i, String letra, boolean numerado) {
        if (!numerado) return null;

        if (letra == null || letra.equals("null") || letra.equals("undefined") || letra.equals(" ")) {
            return Integer.toString(i);
        }
        return letra + i;
    }

    @Override
    public ResponseEntity<?> agregarHijos(Long pIdTicketPadre, Integer pCantidad) {
        Ticket ticketPadre = this.findById(pIdTicketPadre);
        if (ticketPadre == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Configurar factory y crear nuevos asientos adicionales
        ticketFactory.setLocalidad(ticketPadre.getLocalidad());
        ticketFactory.setNumeroTicket(ticketPadre.getNumero());
        ticketFactory.setTipo(ticketPadre.getTipo());
        ticketFactory.setEstado(ticketPadre.getEstado());

        List<Ticket> nuevosAsientos = ticketFactory.setAsientos(pCantidad + 1, ticketPadre);

        // Guardar los nuevos asientos
        List<Ticket> asientosGuardados = this.saveAll(nuevosAsientos);

        // Agregar a la lista existente de asientos
        List<Ticket> todosLosAsientos = new ArrayList<>(ticketPadre.getAsientos());
        todosLosAsientos.addAll(asientosGuardados);

        ticketPadre.setAsientos(todosLosAsientos);
        this.save(ticketPadre);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional("transactionManager")
    public Map<String, Object> actualizarEstado(Long pId, int estado, boolean forzar) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Ticket> ticketsActualizados = new ArrayList<>();
            List<Long> idsHijosAfectados = new ArrayList<>();
            List<String> ticketsConCliente = new ArrayList<>();
            List<String> ticketsConOrdenes = new ArrayList<>();

            Ticket ticket = repository.findById(pId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún ticket con el id: " + pId));

            if (ticket.getCliente() != null) {
                ticketsConCliente.add("El ticket tiene al cliente " + ticket.getCliente().getNombre() + " asociado/a");
            }

            if (estado == 0 && ticket.getOrdenes() != null && !ticket.getOrdenes().isEmpty()) {
                ticketsConOrdenes.add("y tiene una orden asociada");
            }

            recolectarIdsHijos(ticket, idsHijosAfectados);

            if (!forzar) {
                StringBuilder mensajeFinal = new StringBuilder();
                mensajeFinal.append("Está a punto de cambiar el estado del ticket ").append(ticket.getId());
                if (!ticketsConCliente.isEmpty()) {
                    mensajeFinal.append(". ").append(String.join(". ", ticketsConCliente));
                }

                if (!ticketsConOrdenes.isEmpty()) {
                    if (!ticketsConCliente.isEmpty()) {
                        mensajeFinal.append(" ");
                    } else {
                        mensajeFinal.append(". El ticket ");
                    }
                    mensajeFinal.append(String.join(". ", ticketsConOrdenes));
                    mensajeFinal.append(". Recordatorio: Si desea cambiar algo de una orden, puede hacerlo en gestion de ordenes");
                }

                if (!idsHijosAfectados.isEmpty()) {
                    if (!ticketsConCliente.isEmpty() || !ticketsConOrdenes.isEmpty()) {
                        mensajeFinal.append(" y los tickets ");
                    } else {
                        mensajeFinal.append(". Los tickets ");
                    }
                    mensajeFinal.append(idsHijosAfectados.toString().replace("[", "").replace("]", ""));
                    mensajeFinal.append(" también cambiarán de estado");
                }

                mensajeFinal.append(". ¿Está seguro/a de que desea continuar?");

                response.put("advertencia", mensajeFinal.toString());
                response.put("idsHijosAfectados", idsHijosAfectados);
                response.put("ticketsConCliente", ticketsConCliente);
                response.put("ticketsConOrdenes", ticketsConOrdenes);
                response.put("requiereConfirmacion", true);
                return response;
            }

            if (forzar) {
                ticket.setTarifa(null);
                ticket.setCliente(null);
            }

            ticketsActualizados.addAll(actualizarTicketYHijos(ticket, estado));
            repository.saveAll(ticketsActualizados);

            response.put("ticketsActualizados", ticketsActualizados);
            response.put("exito", true);
            response.put("mensaje", "Estado actualizado correctamente");
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el estado de los tickets: " + e.getMessage(), e);
        }
    }


    private void recolectarIdsHijos(Ticket ticket, List<Long> idsHijos) {
        if (ticket.getAsientos() != null && !ticket.getAsientos().isEmpty()) {
            for (Ticket hijo : ticket.getAsientos()) {
                idsHijos.add(hijo.getId());
                recolectarIdsHijos(hijo, idsHijos); // Recursividad para hijos de hijos
            }
        }
    }

    private List<Ticket> actualizarTicketYHijos(Ticket ticket, int estado) {
        List<Ticket> ticketsParaActualizar = new ArrayList<>();

        // Actualizar el ticket actual
        ticket.setEstado(estado);
        ticketsParaActualizar.add(ticket);

        // Actualizar hijos recursivamente
        if (ticket.getAsientos() != null && !ticket.getAsientos().isEmpty()) {
            for (Ticket hijo : ticket.getAsientos()) {
                hijo.setCliente(null);
                hijo.setTarifa(null);
                hijo.setEstado(estado);
                ticketsParaActualizar.addAll(actualizarTicketYHijos(hijo, estado));
            }
        }

        return ticketsParaActualizar;
    }


    /**
     * {@inheritDoc}
     */
    @Transactional("transactionManager")
    @Override
    public List<Ticket> saveAllKafka(List<Ticket> pTickets) {

        List<Ticket> ticketsGuardados = new ArrayList<>();

        pTickets.forEach(ticket -> {
            ticketsGuardados.add(saveKafka(ticket));
        });

        return ticketsGuardados;
    }


    @Override
    @Transactional("transactionManager")
    public List<Ticket> findTicketsByLocalidadIdAndEstado(Long idLocalidad, int estado, int cantidad) {
        List<Ticket> tickets = repository.findByLocalidadIdAndEstadoLimitedTo(idLocalidad, estado,
                PageRequest.of(0, cantidad));
        return tickets;
    }

    @Override
    @Transactional("transactionManager")
    public Map<String, Object> actualizarTicket(Ticket ticket, boolean forzar) {
        try {
            Map<String, Object> response = new HashMap<>();
            Ticket ticketExistente = repository.findById(ticket.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticket.getId()));

            if (ticket.getTarifa() == null || ticket.getTarifa().getId() == null) {
                throw new IllegalArgumentException("La tarifa del ticket no puede ser nula");
            }

            // Listas para advertencias
            List<String> advertencias = new ArrayList<>();
            List<Long> idsHijosAfectados = new ArrayList<>();
            List<String> ticketsConCliente = new ArrayList<>();

            // Verificar tickets con cliente asociado
            if (ticketExistente.getCliente() != null && !forzar) {
                ticketsConCliente.add("El ticket " + ticketExistente.getId() +
                        " tiene cliente asociado (" + ticketExistente.getCliente().getNombre() + ")");
            }


            // Recolectar hijos afectados
            if (ticketExistente.getAsientos() != null && !ticketExistente.getAsientos().isEmpty()) {
                ticketExistente.getAsientos().forEach(hijo -> {
                    idsHijosAfectados.add(hijo.getId());
                    if (hijo.getCliente() != null && !forzar) {
                        ticketsConCliente.add("El asiento " + hijo.getId() +
                                " tiene cliente asociado (" + hijo.getCliente().getNombre() + ")");
                    }
                });
            }

            // Construir mensaje combinado si hay advertencias
            if ((!ticketsConCliente.isEmpty() || !idsHijosAfectados.isEmpty()) && !forzar) {
                StringBuilder mensaje = new StringBuilder();

                if (!ticketsConCliente.isEmpty()) {
                    mensaje.append(String.join(". ", ticketsConCliente));
                }

                if (!idsHijosAfectados.isEmpty()) {
                    if (mensaje.length() > 0) {
                        mensaje.append(" y ");
                    }
                    mensaje.append(idsHijosAfectados.size() + " tickets aparte de este serán modificados");
                }

                mensaje.append(". ¿Desea continuar?");

                response.put("advertencia", mensaje.toString());
                response.put("idsHijosAfectados", idsHijosAfectados);
                response.put("ticketsConCliente", ticketsConCliente);
                return response;
            }

            // Actualización real
            List<Ticket> ticketsParaActualizar = new ArrayList<>();

            // Actualizar ticket principal
            ticketExistente.setNumero(ticket.getNumero());
            ticketExistente.setTarifa(ticket.getTarifa());
            ticketsParaActualizar.add(ticketExistente);

            // Actualizar asientos - IMPORTANTE: Obtener los hijos actualizados de la base de datos
            List<Ticket> hijosActualizados = repository.findAllById(idsHijosAfectados);
            hijosActualizados.forEach(hijo -> {
                hijo.setTarifa(ticket.getTarifa());
                hijo.setNumero(ticket.getNumero());
                ticketsParaActualizar.add(hijo);
            });

            repository.saveAll(ticketsParaActualizar);
            response.put("ticketActualizado", ticketExistente);
            response.put("ticketsHijosActualizados", hijosActualizados.size());
            return response;

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el ticket: " + e.getMessage(), e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarSiNoTieneOrdenes(Long pId) {
        Ticket ticket = repository.findById(pId).orElseThrow(() ->
                new IllegalArgumentException("Ticket no encontrado con id: " + pId));

        if (ticket.getOrdenes() != null && !ticket.getOrdenes().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el ticket porque tiene órdenes asociadas.");
        }

        repository.delete(ticket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("transactionManager")
    public String agregarTicketACliente(Long pIdTicket, Cliente pCliente, String token) throws Exception {

        Ticket ticket = repository.findById(pIdTicket).orElse(null);
        Integer estado = ticket.getEstado();

        // Si el cliente del ticket es diferente al del token y el usuario no es
        // administrador, no permitir hacer nada
        if (!pCliente.getNumeroDocumento().equals(clienteService.obtenerUsuarioDeToken(token))
                    && !clienteService.obtenerRolDeToken(token).equals("ROLE_ADMIN")) {
          return "No se pudo agregar el ticket a cliente";
        }

        String retorno = "Agregado el cliente exitosamente";

        Localidad localidad = ticket.getLocalidad();

        List<Ticket> tickets = new ArrayList<>();

        //Vender ticket principal
        ticket.vender(pCliente, localidad.getTarifaActiva());

        tickets.add(ticket);

        //Vender todos los asientos
       if( ticket.getTipo() == 0 && ticket.getAsientos() != null && !ticket.getAsientos().isEmpty())
       {
           ticket.getAsientos().forEach(asiento -> {
               asiento.vender(pCliente, localidad.getTarifaActiva());
               tickets.add(asiento);
           });
       }

       //Publicar en KAFKA Enviar todos los qrs
       enviar(tickets);

        if (estado == 1) {
            retorno = "Se agrego el cliente, ten en cuenta que estaba previamente vendido";
        } else if (estado == 3) {
            retorno = "Se agrego el cliente, ten en cuenta que estaba en proceso de venta";
        }

        return retorno;
    }

    public void mandarQR(Ticket pTicket) {
        try {

            Localidad localidad = pTicket.getLocalidad();

            Evento evento = eventoService.findByLocalidadId(localidad.getId());

            Cliente cliente = pTicket.getCliente();

            String filepath = QR_CODE_IMAGE_PATH + "Ticket" + pTicket.getId() + "," + pTicket.getCliente().getNumeroDocumento() + ".png";

            //Obtener ingresos del ticket
            List<Ingreso> ingresos = pTicket.getIngresos();

            //MANDAR QR por cada ingreso del ticket
            ingresos.forEach(ingreso -> {

                try {
                    String contenidoQR = "INGRESO:" + ingreso.getId() + "," + cliente.getNumeroDocumento() + "," + evento.getId();

                    String contenido = encriptador.encrypt(contenidoQR);

                    QRCodeGenerator.generateQRCodeImage(contenido, 400, 400, filepath);

                    File file = new File(filepath);
                    FileInputStream input = new FileInputStream(file);

                    MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", IOUtils.toByteArray(input));

                    String nombre = awsService.uploadFile(multipartFile);
                    String src = "https://marcablanca.allticketscol.com/" + nombre;
                    String path = "MARCABLANCA" + pTicket.getId() + pTicket.getCliente().getNumeroDocumento() + System.currentTimeMillis() + "_" + "ticket.pdf";

                    servicioPDF.generatePdfFileTicket("ticketMarcaBlanca", ingreso, path, src, evento, localidad);

                    File file2 = new File(QR_CODE_IMAGE_PATH + path);

                    String numeroTicket = pTicket.getNumero();

                    if (numeroTicket == null) {
                        numeroTicket = "" + pTicket.getId();
                    }

                    emailService.mandarCorreo(numeroTicket, cliente.getCorreo(), file2);

                } catch (Exception e) {

                    logger.error("Error generando y enviando QR para el ingreso {}: {}", ingreso.getId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            logger.error("Error en mandarQR para el ticket {}: {}", pTicket.getId(), e.getMessage(), e);
        }
    }

    @Transactional("transactionManager")
    public void publicarModificacionDeTicket(Ticket pTicket) {
        Ticket ticketBd = repository.save(pTicket);
    }

    @Override
    public List<Ticket> findAllByLocalidad(Long pLocalidadId) {
        return repository.findByLocalidadId(pLocalidadId);
    }

    //----------------Métodos para Kafka-------------------

    /**
     * Guarda un ticket y publica el evento en Kafka
     * @param pTicket El ticket a guardar
     * @return El ticket guardado
     */
    @Transactional("transactionManager")
    @Override
    public Ticket saveKafka(Ticket pTicket) {
        Ticket ticket = this.save(pTicket);

        TicketEvent ticketEvent = new TicketEvent(
                ticket.getId(),
                ticket.getNumero(),
                ticket.getEstado(),
                ticket.getTipo(),
                ticket.getLocalidad() != null ? ticket.getLocalidad().getId() : null,
                ticket.getTarifa() != null ? ticket.getTarifa().getId() : null,
                ticket.getCliente() != null ? ticket.getCliente().getNumeroDocumento() : null,
                ticket.getSeguro() != null ? ticket.getSeguro().getId() : null,
                ticket.getPalco() != null ? ticket.getPalco().getId() : null
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ticketsTopic, "Ticket-" + ticketEvent.getId(), ticketEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        return ticket;
    }

    /**
     * Elimina un ticket por su ID y publica el evento de eliminación en Kafka
     * @param pId El ID del ticket a eliminar
     */
    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        Ticket ticket = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún ticket con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ticketsTopic, "Ticket-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

    @Override
    @Transactional("transactionManager")
    public void crearTicketsReporte(List<Ticket> tickets, Long localidadId) {
        logger.info("Iniciando envío de reporte de tickets. Cantidad: {}, LocalidadId: {}", tickets.size(), localidadId);
        try {
            // Preparar tickets para reporte inicializando atributos transient
            tickets.forEach(ticket -> {
                ticket.setIngresosReporte();
                ticket.setAsientosReporte();
            });
            
            ResponseEntity<?> response = reporteFeignClient.crearTicketsReporte(tickets, localidadId);

            logger.info("Respuesta del microservicio de reportes: {}", response.getStatusCode());

            if (response.getBody() != null) {
                logger.debug("Cuerpo de respuesta: {}", response.getBody());
            }

        } catch (Exception e) {
            logger.error("Error en la llamada Feign al microservicio de reportes: {}", e.getMessage(), e);
            throw e; // Re-lanzar para que se maneje en el nivel superior
        }
    }


    public void enviar(List<Ticket> tickets) throws Exception {
        tickets.forEach(ticket -> {
            if(ticket.isVendido() && ticket.getCliente() != null){
                try {
                    saveKafka(ticket);
                    mandarQR(ticket);
                } catch (Exception e) {
                    logger.error("Error al enviar QR del ticket {}: {}", ticket.getId(), e.getMessage());
                }
            }
        });
    }

    public Integer validarVentasCupon(Long pTarifaId){
        //Contar cuantos tickets hay vendidos con la tarifa indicada
        return repository.countByTarifaIdAndEstado(pTarifaId,1);
    }

    @Override
    public List<MisTicketsDto> getMisTicketsByCliente(String numeroDocumento) {
        //nombrar al método findByClienteNumeroDocumentoAnd - Evento- EstadoNoT falta especificar que te refieres al estado del evento
        List<Ticket> tickets = repository.findByClienteNumeroDocumentoAndEventoEstadoNot(numeroDocumento,3);
        return MisTicketsDto.TicketsToDto(tickets);
    }


}
