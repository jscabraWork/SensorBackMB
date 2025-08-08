package com.arquitectura.clients;

import com.arquitectura.ticket.entity.Ticket;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="microservicio-reportes", configuration = FeignClientConfig.class)
public interface ReporteFeignClient {

    @PostMapping("/tickets/crear/{pLocalidadId}")
    public ResponseEntity<?> crearTicketsReporte(@RequestBody List<Ticket> pTickets, @PathVariable Long pLocalidadId);

}
