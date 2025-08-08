package com.arquitectura.pdf;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.ticket.entity.Ticket;
import com.lowagie.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService implements PdfGenerateService {

    private Logger logger = LoggerFactory.getLogger(PdfGenerateService.class);

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private AWSS3Service awsService;

    private static final String QR_CODE_IMAGE_PATH = "./uploads/";

    @Override
    public void generatePdfFileTicket(String templateName, Ticket ticket, String pdfFileName, String imagen,
                                      Evento evento, Localidad localidad){

        Context context = new Context();

        Map<String, Object> data = new HashMap<>();

        int year = 0;
        int month = 0;
        int day = 0;
        String fecha = "";
        String localidadString=localidad.getNombre();

        if(ticket.getServicios().size()>0) {
            for(int i =0; i < ticket.getServicios().size();i++) {
                localidadString+=" " + ticket.getServicios().get(i).getNombre();
            }
        }

        List<Dia> dias = evento.getDias();
        List<String> diasInfo = new ArrayList<>();

        if (dias != null && !dias.isEmpty()) {
            diasInfo = dias.stream()
                    .map(dia -> {
                        String info = dia.getNombre();
                        if (dia.getHoraInicio() != null && !dia.getHoraInicio().isEmpty()) {
                            info += " - " + dia.getHoraInicio();
                        } else {
                            info += " - Pendiente por confirmar";
                        }
                        return info;
                    })
                    .collect(Collectors.toList());
        } else {
            diasInfo.add("Pendiente por confirmar");
        }

        if (ticket.getIngresos().get(0).getDia()!= null) {
            year = ticket.getIngresos().get(0).getDia().getFechaInicio().getYear();
            month = ticket.getIngresos().get(0).getDia().getFechaInicio().getMonthValue();
            day = ticket.getIngresos().get(0).getDia().getFechaInicio().getDayOfMonth();
            fecha = year + "-" + month + "-" + day;
        }  else {
            fecha = "Fecha: Por confirmar";
        }

        String imgFondo = "https://allticketscol.com/assets/images/img/concierto.jpg";

        String id = ticket.getId().toString();
        if (ticket.getNumero() != null) {
            id = ticket.getNumero();
        }

        String cantidadP = "1";

        data.put("fecha", fecha);
        data.put("localidad", localidadString);
        data.put("cliente", ticket.getCliente());
        data.put("evento", evento);
        data.put("boleta", ticket);
        data.put("cantidadP", cantidadP);
        data.put("id", id);
        data.put("imgFondo", imgFondo);
        data.put("imagen",imagen);
        data.put("diasInfo",diasInfo);
        System.out.println("toda la informacion para la plantilla html para enviar QR: " + data);
        context.setVariables(data);
        String htmlContent = templateEngine.process(templateName, context);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(QR_CODE_IMAGE_PATH + pdfFileName);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            renderer.createPDF(fileOutputStream,false);
            renderer.finishPDF();

        } catch (FileNotFoundException e) {

            logger.error(e.getMessage(), e);
        } catch (DocumentException e) {

            logger.error(e.getMessage(), e);
        }

    }

    public String subirPDF(MultipartFile file) {

        String nombre = awsService.uploadFile(file);

        String src = "https://codigos.allticketscol.com/" + nombre;

        return src;
    }
}
