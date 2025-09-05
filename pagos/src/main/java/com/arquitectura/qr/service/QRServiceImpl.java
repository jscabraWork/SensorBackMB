package com.arquitectura.qr.service;

import com.arquitectura.aws.AWSS3Service;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.mail.service.SendEmailAmazonServiceImpl;
import com.arquitectura.pdf.PdfService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketServiceImpl;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class QRServiceImpl implements QRService{

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Autowired
    private EventoService eventoService;

    private static final String QR_CODE_IMAGE_PATH = "./uploads/";

    @Autowired
    private SendEmailAmazonServiceImpl emailService;

    @Autowired
    private EncriptarTexto encriptador;

    @Autowired
    private AWSS3Service awsService;

    @Autowired
    private PdfService servicioPDF;

    public void mandarQR(Ticket pTicket) {
        try {

            Localidad localidad = pTicket.getLocalidad();

            Evento evento = eventoService.findByLocalidadId(localidad.getId());

            Cliente cliente = pTicket.getCliente();

            String filepath = QR_CODE_IMAGE_PATH + "Ticket" + pTicket.getId() + "," + pTicket.getCliente().getNumeroDocumento() + ".png";

            //Obtener ingresos del ticket
            List<Ingreso> ingresos = pTicket.getIngresos();

            List<File> ticketsPDF = new ArrayList<>();

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
                    String src = "https://codigos.ticketsensor.com/" + nombre;
                    String path = "TICKET_SENSOR_" + pTicket.getId() + pTicket.getCliente().getNumeroDocumento() + System.currentTimeMillis() + "_" + "ticket.pdf";

                    servicioPDF.generatePdfFileTicket("ticketMarcaBlanca", ingreso, path, src, evento, localidad);

                    File pdfFile = new File(QR_CODE_IMAGE_PATH + path);

                    ticketsPDF.add(pdfFile);

                } catch (Exception e) {
                    logger.error("Error generando y enviando QR para el ingreso {}: {}", ingreso.getId(), e.getMessage(), e);
                }
            });

            //Numero de ticket
            String numeroTicket = pTicket.getNumero();
            if (numeroTicket == null) {
                numeroTicket = "" + pTicket.getId();
            }

            //Enviar correo
            emailService.mandarCorreo(numeroTicket, cliente.getCorreo(), ticketsPDF);

        } catch (Exception e) {
            logger.error("Error en mandarQR para el ticket {}: {}", pTicket.getId(), e.getMessage(), e);
        }
    }
}
