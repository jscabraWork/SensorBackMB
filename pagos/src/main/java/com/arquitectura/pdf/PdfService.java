package com.arquitectura.pdf;

import com.arquitectura.aws.AWSS3Service;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.ingreso.entity.Ingreso;
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
    public void generatePdfFileTicket(String templateName, Ingreso ingreso, String pdfFileName, String imagenQR,
                                      Evento evento, Localidad localidad){

        Context context = new Context();

        Ticket ticket = ingreso.getTicket();

        Map<String, Object> data = new HashMap<>();

        int year = 0;
        int month = 0;
        int day = 0;
        String hora ="";
        String fecha = "";
        String localidadString=localidad.getNombre();

        if(ticket.getServicios().size()>0) {
            for(int i =0; i < ticket.getServicios().size();i++) {
                localidadString+=" " + ticket.getServicios().get(i).getNombre();
            }
        }

        Dia dia = ingreso.getDia();

        hora = dia.getNombre();

        if (dia.getHoraInicio()!=null) {
            hora = hora + " " + dia.getHoraInicio();
        } else {
            hora = hora + " Por confirmar";
        }

        if (dia.getFechaInicio() !=null ) {
            year = dia.getFechaInicio().getYear();
            month = dia.getFechaInicio().getMonthValue();
            day = dia.getFechaInicio().getDayOfMonth();
            fecha = year + "-" + month + "-" + day;
        }  else {
            fecha = "Por confirmar";
        }

        String imgFondo = "https://allticketscol.com/assets/images/img/concierto.jpg";

        Imagen imagen = evento.getImagenByTipo(3);

        if(imagen != null) {
        	imgFondo = imagen.getUrl();
        }

        String id = ticket.getId().toString();
        if (ticket.getNumero() != null) {
            id = ticket.getNumero();
        }

        data.put("fecha", fecha);
        data.put("localidad", localidadString);
        data.put("cliente", ticket.getCliente());
        data.put("evento", evento);
        data.put("boleta", ticket);
        data.put("cantidadP", "1");
        data.put("id", id);
        data.put("imgFondo", imgFondo);
        data.put("imagen",imagenQR);
        data.put("hora",hora);
        data.put("dia",dia.getNombre());
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
