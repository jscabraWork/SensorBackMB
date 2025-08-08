package com.arquitectura.pdf;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;

public interface PdfGenerateService {

    void generatePdfFileTicket(String templateName, Ticket boleta, String pdfFileName,String imagen, Evento evento, Localidad localidad);
}
