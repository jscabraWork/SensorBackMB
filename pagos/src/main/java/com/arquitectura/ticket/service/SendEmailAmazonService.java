package com.arquitectura.ticket.service;

import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

@Service
public class SendEmailAmazonService {

    static final String FROM = "noreply_tickets@allticketscol.com";
    static final String FROMNAME = "MarcaBlanca_NO_RESPONDER";
    static final String SMTP_USERNAME = "AKIAZ4YTFVLRGWUACVUD";
    static final String SMTP_PASSWORD = "BPNnz1m2Tu7EMWoC7t0iDH7ap8cefA3OiFhtODMhbKtV";
    static final String HOST = "email-smtp.us-east-1.amazonaws.com";
    static final int PORT = 587;

    public void mandarCorreo(String numero, String to, int cantidadPersonas, File adjunto) throws Exception {
        System.out.println("Entramos al método para mandar el correo...............");

        // DEBUGGING BÁSICO DEL ARCHIVO
        System.out.println("=== DEBUG ARCHIVO ===");
        System.out.println("Archivo: " + adjunto.getName());
        System.out.println("Ruta completa: " + adjunto.getAbsolutePath());
        System.out.println("¿Existe?: " + adjunto.exists());

        System.out.println("=== FIN DEBUG ===");

        if (!adjunto.exists()) {
            throw new IllegalArgumentException("El archivo no existe: " + adjunto.getAbsolutePath());
        }

        String SUBJECT = "Ticket Numero: " + numero;
        String BODY = "<body>" +
                "<section style=\"width: 100%; font-family: Arial, Helvetica, sans-serif;\">" +
                "<div style=\"background-color: #102438; text-align: center; box-shadow: rgba(0, 0, 0, 0.25) 0px 14px 28px, rgba(0, 0, 0, 0.22) 0px 10px 10px;\">" +
                "<img style=\"width: 300px; padding: 30px 0 30px 0;\" src=\"\" alt=\"logo\">" +
                "</div>" +
                "<div style=\"text-align: center; padding: 30px 15px;\">" +
                "<p>🎟️<b> Gracias por confiar en Marca Blanca </b>🎟️</p>" +
                "<p>En el archivo adjunto está tu ticket QR. <br>¡Preséntalo desde tu celular en la entrada del evento y listo!</p>" +
                "<p>¡Vívelo al máximo!</p>" +
                "</div>" +
                "<div style=\"background-color: #505050; height: auto; text-decoration: none; text-align: center; padding: 15px 20px; box-shadow: rgba(0, 0, 0, 0.25) 0px 14px 28px, rgba(0, 0, 0, 0.22) 0px 10px 10px;\">" +
                "<p style=\"color: white; text-align: center;\">" +
                "<b>Este correo es automático.</b><br> Si necesitas asistencia, por favor contáctanos <br>al WhatsApp +57 305 371 0154" +
                "</p>" +
                "<p style=\"color: white; text-align: center; font-size: 12px;\">" +
                "Aplican Términos y Condiciones: <a style=\"color: white; text-decoration: none;\" href=\"https://marcablanca.com/terminosYcondiciones\">https://marcablanca.com/terminosYcondiciones</a>" +
                "</p>" +
                "</div>" +
                "</section>" +
                "</body>";

        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.


        BodyPart  mensaje = new MimeBodyPart();
        mensaje.setContent(BODY,"text/html; charset=utf-8");

        MimeBodyPart archivoAdjunto = new MimeBodyPart();
        FileDataSource fuenteArchivosDatos = new FileDataSource(adjunto);
        DataHandler manejadorDatos = new DataHandler(fuenteArchivosDatos);
        archivoAdjunto.setDataHandler(manejadorDatos);
        MimeMultipart multiParte = new MimeMultipart();
        archivoAdjunto.setFileName(adjunto.getName());

        multiParte.addBodyPart(mensaje);
        multiParte.addBodyPart(archivoAdjunto);


        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(multiParte);


        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
        //msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try
        {
            System.out.println("Sending...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {

            // Close and terminate the connection.
            transport.close();
        }
    }
}