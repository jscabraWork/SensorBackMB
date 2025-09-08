package com.arquitectura.tarea;

import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.ptp.PlaceToPlayService;
import com.arquitectura.ptp.PtpAdapter;
import com.arquitectura.ptp.RequestResponse;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Component
public class TareaRecurrente {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private PlaceToPlayService placeToPlayService;

    @Autowired
    private PtpAdapter adapter;

    @Autowired
    private TransaccionService transaccionService;


    @Scheduled(fixedRate=900000)
    @Transactional("transactionManager")
    public void revisarOrdenesEnProceso() {

        System.out.println("Revisando ordenes en proceso...");

        List<Orden> ordenes= ordenService.findByEstado(3);

        LocalDateTime fechaActual = LocalDateTime.now();

        ordenes.forEach(orden -> {

            LocalDateTime fechaCreacionOrden = orden.getCreationDate();

            //Es la fecha actual de la orden mas 30 minutos
            LocalDateTime fechaCancelacionOrden = fechaCreacionOrden.plusMinutes(30);

            Boolean fechaVencida = fechaActual.isAfter(fechaCancelacionOrden);

            //Si la fecha esta vencida y no tiene requestId simplemente se rechaza la orden
            if(fechaVencida && orden.getIdTRXPasarela()==null) {
                //Estas ordenes nunca pasaron al checkout o son de traspasoz
                orden.rechazar();
                ordenService.saveKafka(orden); //GUARDAR LA ORDEN RECHAZADA
                ticketService.saveAllKafka(orden.getTickets()); //GUARDAR LOS TICKETS ASOCIADOS A LA ORDEN RECHAZADA
                transaccionService.saveAllKafka(orden.getTransacciones()); //GUARDAR LAS TRANSACCIONES ASOCIADAS A LA ORDEN RECHAZADA
            }
            else if(fechaVencida && orden.getIdTRXPasarela()!=null)
            {
                try {
                    //Obtener la transaccion Consultando contra PTP
                    Transaccion transaccion = placeToPlayService.generarTransaccionPTP(orden.getIdTRXPasarela(),orden.getId());

                    //La transaccion devuelve null si esta repetida
                    //En este caso no se debe hacer nada
                    if (transaccion != null) {

                        //Asignar la orden a la transaccion y guardar la transaccion
                        transaccion.setOrden(orden);
                        Transaccion transaccionBD = transaccionService.saveKafka(transaccion);

                        //Si la transaccion es aprobada recibirla
                        if (transaccionBD.isAprobada()) {
                            placeToPlayService.procesarTransaccionExitosa(orden, transaccionBD);
                        }
                        //Si la transaccion es pendiente actualizar la orden
                        else if( transaccionBD.isPendiente()){
                            //Actualizar lastModifiedDate de la orden a la fecha actual
                            orden.setLastModifiedDate(fechaActual);
                            ordenService.save(orden);
                        }
                        else{ //Si la orden no es aprobada ni pendiente, rechazar la orden
                            orden.rechazar();
                            ordenService.saveKafka(orden);
                            ticketService.saveAllKafka(orden.getTickets());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Tarea recurrente diaria
    @Transactional("transactionManager")
    @Scheduled(cron = "0 17 2 * * *")
    public void revisionDeTransaccionesPendientesDiario() {

        List<Orden> ordenes= ordenService.findAllOrdenesSinConfirmacion();

        ordenes.forEach(orden -> {
            placeToPlayService.manejarTransaccionConPtp(orden);
        });
    }

}
