package com.arquitectura.pdf;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;

public interface PdfGenerateService {

    void generatePdfFileTicket(String templateName, Ingreso ingreso, String pdfFileName, String imagen, Evento evento, Localidad localidad);
}
