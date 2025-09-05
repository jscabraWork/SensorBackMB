package com.arquitectura.mail.service;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.codigo_traspaso.entity.CodigoTraspaso;
import com.arquitectura.evento.entity.Evento;
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
import java.util.List;
import java.util.Properties;

@Service
public class SendEmailAmazonServiceImpl {

    static final String FROM = "no-replay@ticketsensor.com";
    static final String FROMNAME = "SENSOR_NO_RESPONDER";
    static final String SMTP_USERNAME = "AKIAZ4YTFVLRGWUACVUD";
    static final String SMTP_PASSWORD = "BPNnz1m2Tu7EMWoC7t0iDH7ap8cefA3OiFhtODMhbKtV ";
    static final String HOST = "email-smtp.us-east-1.amazonaws.com";
    static final int PORT = 587;

    public void mandarCorreo(String numero, String to, List<File> adjuntos) throws Exception {
        String SUBJECT = "Ticket Sensor Events: " + numero;

        String BODY = String.join(
                System.getProperty("line.separator"),
                "<body style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 0; background-color: #1a1a1a; color: #ffffff;\">\r\n"
                        + "    <section style=\"width: 100%; margin: 0 auto;\">\r\n"
                        + "        <div style=\"background-color: #FFFFFF; text-align: center; padding: 20px 0;\">\r\n"
                        + "            <img style=\"width: 150px;\" src=\"https://codigos.allticketscol.com/sensorImg.png\" alt=\"Sensor Events\">\r\n"
                        + "            <h2 style=\"color: #1a1a1a; margin: 10px 0 0 0;\">SENSOR EVENTS</h2>\r\n"
                        + "        </div>\r\n"
                        + "        <div style=\"text-align: center; padding: 30px 15px; background-color: #222222;\">\r\n"
                        + "            <p style=\"font-size: 18px; margin-bottom: 30px;\">\r\n"
                        + "                <b>UNA DIMENSIÓN SENSORIAL PARA SENTIR, VIVIR Y SER LIBRES</b>\r\n"
                        + "            </p>\r\n"
                        + "            <div style=\"background-color: #333333; padding: 20px; border-left: 5px solid #FFD900;\">\r\n"
                        + "                <p style=\"font-size: 16px; line-height: 1.5;\">\r\n"
                        + "                    En el archivo adjunto está tu ticket QR. <br>\r\n"
                        + "                    Preséntalo desde tu celular en la entrada del evento y ¡disfruta de una experiencia sensorial única!\r\n"
                        + "                </p>\r\n"
                        + "            </div>\r\n"
                        + "            <p style=\"font-size: 22px; margin-top: 30px; color: #FFD900;\">\r\n"
                        + "                ¡UNA DIMENSIÓN SENSORIAL!\r\n"
                        + "            </p>\r\n"
                        + "        </div>\r\n"
                        + "        <div style=\"background-color: #333333; padding: 20px 15px; text-align: center;\">\r\n"
                        + "            <p style=\"color: #ffffff; text-align: center;\">\r\n"
                        + "                <b>Este correo es automático.</b><br> Si necesitas asistencia, por favor contáctanos <br>al WhatsApp +57\r\n"
                        + "                321 918 7944\r\n"
                        + "            </p>\r\n"
                        + "            <div style=\"margin: 20px 0;\">\r\n"
                        + "                <a href=\"https://api.whatsapp.com/send?phone=573219187944\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/3670/3670051.png\" alt=\"WhatsApp\">\r\n"
                        + "                </a>\r\n"
                        + "                <a href=\"https://www.instagram.com/sensor_events/?hl=es-la\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/4138/4138124.png\" alt=\"Instagram\">\r\n"
                        + "                </a>\r\n"
                        + "                <a href=\"https://web.facebook.com/sensorevents?_rdc=1&_rdr\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/5968/5968764.png\" alt=\"Facebook\">\r\n"
                        + "                </a>\r\n"
                        + "            </div>\r\n"
                        + "            <p style=\"color: #cccccc; text-align: center; font-size: 12px;\">\r\n"
                        + "                Aplican Términos y Condiciones:<br>\r\n"
                        + "                <a style=\"color: #FFD900; text-decoration: none;\" href=\"https://ticketsensor.com/terminos\">https://sensorevents.com/terminos</a>\r\n"
                        + "            </p>\r\n"
                        + "        </div>\r\n"
                        + "    </section>\r\n"
                        + "</body>"
        );

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        BodyPart mensaje = new MimeBodyPart();
        mensaje.setContent(BODY,"text/html; charset=utf-8");

        MimeMultipart multiParte = new MimeMultipart();
        multiParte.addBodyPart(mensaje);

        for (File adjunto : adjuntos) {
            MimeBodyPart archivoAdjunto = new MimeBodyPart();
            FileDataSource fuenteArchivosDatos = new FileDataSource(adjunto);
            DataHandler manejadorDatos = new DataHandler(fuenteArchivosDatos);
            archivoAdjunto.setDataHandler(manejadorDatos);
            archivoAdjunto.setFileName(adjunto.getName());
            multiParte.addBodyPart(archivoAdjunto);
        }

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(multiParte);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            System.out.println("Enviando correo...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("¡Correo enviado exitosamente!");
        } catch (Exception ex) {
            System.out.println("El correo no pudo ser enviado.");
            System.out.println("Mensaje de error: " + ex.getMessage());
            throw ex;
        } finally {
            // Close and terminate the connection.
            transport.close();
        }
    }

    public void mandarCorreoCederTicket(CodigoTraspaso codigo) throws Exception {

        Cliente cliente = codigo.getCliente();

        String SUBJECT = cliente.getNombre() + " te esta cediendo un ticket";

        Evento evento = codigo.getTicket().getEvento();

        String enlace = "https://ticketsensor.com/confirmar-traspaso/" + codigo.getCodigo();

        String to = codigo.getCorreoDestino();

        String BODY = String.join(
                System.getProperty("line.separator"),
                "<body style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 0; background-color: #1a1a1a; color: #ffffff;\">\r\n"
                        + "    <section style=\"width: 100%; margin: 0 auto;\">\r\n"
                        + "        <div style=\"background-color: #FFFFFF; text-align: center; padding: 20px 0;\">\r\n"
                        + "            <img style=\"width: 150px;\" src=\"https://codigos.allticketscol.com/sensorImg.png\" alt=\"Sensor Events\">\r\n"
                        + "            <h2 style=\"color: #1a1a1a; margin: 10px 0 0 0;\">SENSOR EVENTS</h2>\r\n"
                        + "        </div>\r\n"
                        + "        <div style=\"text-align: center; padding: 30px 15px; background-color: #222222;\">\r\n"
                        + "            <p style=\"font-size: 18px; margin-bottom: 30px;\">\r\n"
                        + "                <b>UNA DIMENSIÓN SENSORIAL PARA SENTIR, VIVIR Y SER LIBRES</b>\r\n"
                        + "            </p>\r\n"
                        + "            <div style=\"background-color: #333333; padding: 20px; border-left: 5px solid #FFD900;\">\r\n"
                        + "                <h3 style=\"color: #FFD900; margin-top: 0; font-size: 20px;\">\r\n"
                        + "                    ¡Te están cediendo un ticket!\r\n"
                        + "                </h3>\r\n"
                        + "                <p style=\"font-size: 16px; line-height: 1.5; margin-bottom: 20px;\">\r\n"
                        + "                    <b>" + cliente.getNombre() + "</b> te está traspasando un ticket para el evento:<br>\r\n"
                        + "                    <span style=\"color: #FFD900; font-size: 18px;\"><b>" + evento.getNombre() + "</b></span>\r\n"
                        + "                </p>\r\n"
                        + "                <p style=\"font-size: 14px; color: #cccccc; margin-bottom: 25px;\">\r\n"
                        + "                    Haz clic en el siguiente botón para confirmar el traspaso y recibir tu ticket:\r\n"
                        + "                </p>\r\n"
                        + "                <div style=\"text-align: center; margin: 25px 0;\">\r\n"
                        + "                    <a href=\"" + enlace + "\" style=\"display: inline-block; background-color: #FFD900; color: #1a1a1a; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; transition: background-color 0.3s;\">\r\n"
                        + "                        CONFIRMAR TRASPASO\r\n"
                        + "                    </a>\r\n"
                        + "                </div>\r\n"
                        + "                <p style=\"font-size: 12px; color: #999999; margin-top: 20px;\">\r\n"
                        + "                    Si no puedes hacer clic en el botón, copia y pega este enlace en tu navegador:<br>\r\n"
                        + "                    <a href=\"" + enlace + "\" style=\"color: #FFD900; word-break: break-all;\">" + enlace + "</a>\r\n"
                        + "                </p>\r\n"
                        + "            </div>\r\n"
                        + "            <p style=\"font-size: 22px; margin-top: 30px; color: #FFD900;\">\r\n"
                        + "                ¡UNA DIMENSIÓN SENSORIAL!\r\n"
                        + "            </p>\r\n"
                        + "        </div>\r\n"
                        + "        <div style=\"background-color: #333333; padding: 20px 15px; text-align: center;\">\r\n"
                        + "            <p style=\"color: #ffffff; text-align: center;\">\r\n"
                        + "                <b>Este correo es automático.</b><br> Si necesitas asistencia, por favor contáctanos <br>al WhatsApp +57\r\n"
                        + "                321 918 7944\r\n"
                        + "            </p>\r\n"
                        + "            <div style=\"margin: 20px 0;\">\r\n"
                        + "                <a href=\"https://api.whatsapp.com/send?phone=573219187944\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/3670/3670051.png\" alt=\"WhatsApp\">\r\n"
                        + "                </a>\r\n"
                        + "                <a href=\"https://www.instagram.com/sensor_events/?hl=es-la\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/4138/4138124.png\" alt=\"Instagram\">\r\n"
                        + "                </a>\r\n"
                        + "                <a href=\"https://web.facebook.com/sensorevents?_rdc=1&_rdr\" style=\"display: inline-block; margin: 0 10px;\">\r\n"
                        + "                    <img style=\"width: 30px;\" src=\"https://cdn-icons-png.flaticon.com/512/5968/5968764.png\" alt=\"Facebook\">\r\n"
                        + "                </a>\r\n"
                        + "            </div>\r\n"
                        + "            <p style=\"color: #cccccc; text-align: center; font-size: 12px;\">\r\n"
                        + "                Aplican Términos y Condiciones:<br>\r\n"
                        + "                <a style=\"color: #FFD900; text-decoration: none;\" href=\"https://ticketsensor.com/terminos\">https://sensorevents.com/terminos</a>\r\n"
                        + "            </p>\r\n"
                        + "        </div>\r\n"
                        + "    </section>\r\n"
                        + "</body>"
        );

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        BodyPart mensaje = new MimeBodyPart();
        mensaje.setContent(BODY,"text/html; charset=utf-8");

        MimeMultipart multiParte = new MimeMultipart();
        multiParte.addBodyPart(mensaje);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(multiParte);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            System.out.println("Enviando correo...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());

            System.out.println("¡Correo enviado exitosamente!");

        } catch (Exception ex) {
            System.out.println("El Correo no pudo ser enviado - error: " + ex.getMessage());
            throw ex;
        } finally {
            // Close and terminate the connection.
            transport.close();
        }
    }

}