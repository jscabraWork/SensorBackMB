package com.arquitectura.mail;


import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class SendEmailAmazonService {

    static final String FROM = "no-replay@ticketsensor.com";
    static final String FROMNAME = "SENSOR_NO_RESPONDER";
    static final String SMTP_USERNAME = "AKIAZ4YTFVLRGWUACVUD";
    static final String SMTP_PASSWORD = "BPNnz1m2Tu7EMWoC7t0iDH7ap8cefA3OiFhtODMhbKtV ";
    static final String HOST = "email-smtp.us-east-1.amazonaws.com";
    static final int PORT = 587;

    public void mandarCorreoContrasenia(String to, String idEncriptado) throws Exception {
        String SUBJECT = "Contraseña recuperada";

        String BODY = String.join(
                System.getProperty("line.separator"),
                "<!DOCTYPE html>\r\n"
                        + "<html lang=\"es\">\r\n"
                        + "<head>\r\n"
                        + "<meta charset=\"UTF-8\">\r\n"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
                        + "<title>Sensor Events - Recuperación de Contraseña</title>\r\n"
                        + "<style>\r\n"
                        + "    body {\r\n"
                        + "        font-family: Arial, sans-serif;\r\n"
                        + "        margin: 0;\r\n"
                        + "        padding: 0;\r\n"
                        + "        background: #1a1a1a;\r\n"
                        + "        text-align: center;\r\n"
                        + "        color: #ffffff;\r\n"
                        + "    }\r\n"
                        + "    .container {\r\n"
                        + "        width: 100%;\r\n"
                        + "        max-width: 900px;\r\n"
                        + "        margin: 50px auto;\r\n"
                        + "        background: #222222;\r\n"
                        + "        padding: 30px;\r\n"
                        + "        box-shadow: 0 0 20px rgba(0,0,0,0.5);\r\n"
                        + "        border-left: 4px solid #FFD900;\r\n"
                        + "    }\r\n"
                        + "    h1 {\r\n"
                        + "        font-size: 24px;\r\n"
                        + "        color: #FFD900;\r\n"
                        + "        margin-top: 20px;\r\n"
                        + "    }\r\n"
                        + "    p {\r\n"
                        + "        font-size: 16px;\r\n"
                        + "        color: #cccccc;\r\n"
                        + "        line-height: 1.5;\r\n"
                        + "        margin-bottom: 20px;\r\n"
                        + "    }\r\n"
                        + "    .boton {\r\n"
                        + "        display: inline-block;\r\n"
                        + "        margin-top: 20px;\r\n"
                        + "        padding: 12px 25px;\r\n"
                        + "        background-color: #FFD900;\r\n"
                        + "        color: #1a1a1a;\r\n"
                        + "        text-decoration: none;\r\n"
                        + "        border-radius: 4px;\r\n"
                        + "        font-weight: bold;\r\n"
                        + "        font-size: 16px;\r\n"
                        + "    }\r\n"
                        + "    .boton:hover {\r\n"
                        + "        background-color: #e5c300;\r\n"
                        + "    }\r\n"
                        + "    .footer {\r\n"
                        + "        margin-top: 40px;\r\n"
                        + "        font-size: 14px;\r\n"
                        + "        color: #777777;\r\n"
                        + "        border-top: 1px solid #333333;\r\n"
                        + "        padding-top: 20px;\r\n"
                        + "    }\r\n"
                        + "    .footer a{\r\n"
                        + "       color: #FFD900;\r\n"
                        + "       text-decoration: none;\r\n"
                        + "    }\r\n"
                        + "    .content-box {\r\n"
                        + "        background-color: #333333;\r\n"
                        + "        padding: 20px;\r\n"
                        + "        margin: 20px 0;\r\n"
                        + "        border-left: 3px solid #FFD900;\r\n"
                        + "    }\r\n"
                        + "    .header-content {\r\n"
                        + "        margin-bottom: 20px;\r\n"
                        + "    }\r\n"
                        + "    .logo {\r\n"
                        + "        width: 100px;\r\n"
                        + "        height: auto;\r\n"
                        + "        margin: 20px 0;\r\n"
                        + "        display: block;\r\n"
                        + "        margin-left: auto;\r\n"
                        + "        margin-right: auto;\r\n"
                        + "    }\r\n"
                        + "</style>\r\n"
                        + "</head>\r\n"
                        + "<body>\r\n"
                        + "<div class=\"container\">\r\n"
                        + "    <div class=\"header-content\">\r\n"
                        + "        <img class=\"logo\" src=\"https://codigos.allticketscol.com/sensorImg.png\" alt=\"Sensor Events Logo\">\r\n"
                        + "    </div>\r\n"
                        + "    <h1>Recuperación de contraseña</h1>\r\n"
                        + "    <div class=\"content-box\">\r\n"
                        + "        <p>Has solicitado un cambio de contraseña. Si no realizaste esta solicitud, te recomendamos cambiar tu correo electrónico asociado a tu cuenta.</p>\r\n"
                        + "    </div>\r\n"
                        + "    <a class=\"boton\" target=\"_blank\" href=\"https://ticketsensor.com/cambio-contrasena/" + idEncriptado + "\">RECUPERAR CONTRASEÑA</a>\r\n"
                        + "    <p class=\"footer\">Cualquier duda que tengas, por favor comunícate con nosotros<br><a target=\"_blank\" href=\"https://api.whatsapp.com/send?phone=573202067543&text=Hola%20Sensor%20Events,%20me%20comunico%20porque:\">+57 320 206 7543</a>.</p>\r\n"
                        + "</div>\r\n"
                        + "</body>\r\n"
                        + "</html>\r\n"
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
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY, "text/html; charset=utf-8");

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            System.out.println("Sending...");
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            transport.close();
        }
    }

    public void mandarCorreoUsuario(String usuario, String idEncriptado, String to) throws Exception {
        String SUBJECT = "Bienvenido a SENSOR - Confirma tu registro";

        String BODY = String.join(
                System.getProperty("line.separator"),
                "<!DOCTYPE html>\r\n"
                        + "<html lang=\"es\">\r\n"
                        + "<head>\r\n"
                        + "<meta charset=\"UTF-8\">\r\n"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
                        + "<title>Bienvenido a Sensor Events</title>\r\n"
                        + "<style>\r\n"
                        + "    body {\r\n"
                        + "        font-family: Arial, sans-serif;\r\n"
                        + "        margin: 0;\r\n"
                        + "        padding: 0;\r\n"
                        + "        background: #1a1a1a;\r\n"
                        + "        text-align: center;\r\n"
                        + "        color: #ffffff;\r\n"
                        + "    }\r\n"
                        + "    .container {\r\n"
                        + "        width: 100%;\r\n"
                        + "        max-width: 900px;\r\n"
                        + "        margin: 50px auto;\r\n"
                        + "        background: #222222;\r\n"
                        + "        padding: 30px;\r\n"
                        + "        box-shadow: 0 0 20px rgba(0,0,0,0.5);\r\n"
                        + "        border-left: 4px solid #FFD900;\r\n"
                        + "    }\r\n"
                        + "    h1 {\r\n"
                        + "        font-size: 24px;\r\n"
                        + "        color: #FFD900;\r\n"
                        + "        margin-top: 20px;\r\n"
                        + "    }\r\n"
                        + "    p {\r\n"
                        + "        font-size: 16px;\r\n"
                        + "        color: #cccccc;\r\n"
                        + "        line-height: 1.5;\r\n"
                        + "        margin-bottom: 20px;\r\n"
                        + "    }\r\n"
                        + "    .boton {\r\n"
                        + "        display: inline-block;\r\n"
                        + "        margin-top: 20px;\r\n"
                        + "        padding: 12px 25px;\r\n"
                        + "        background-color: #FFD900;\r\n"
                        + "        color: #1a1a1a;\r\n"
                        + "        text-decoration: none;\r\n"
                        + "        border-radius: 4px;\r\n"
                        + "        font-weight: bold;\r\n"
                        + "        font-size: 16px;\r\n"
                        + "    }\r\n"
                        + "    .boton:hover {\r\n"
                        + "        background-color: #e5c300;\r\n"
                        + "    }\r\n"
                        + "    .footer {\r\n"
                        + "        margin-top: 40px;\r\n"
                        + "        font-size: 14px;\r\n"
                        + "        color: #777777;\r\n"
                        + "        border-top: 1px solid #333333;\r\n"
                        + "        padding-top: 20px;\r\n"
                        + "    }\r\n"
                        + "    .footer a{\r\n"
                        + "       color: #FFD900;\r\n"
                        + "       text-decoration: none;\r\n"
                        + "    }\r\n"
                        + "    .content-box {\r\n"
                        + "        background-color: #333333;\r\n"
                        + "        padding: 20px;\r\n"
                        + "        margin: 20px 0;\r\n"
                        + "        border-left: 3px solid #FFD900;\r\n"
                        + "    }\r\n"
                        + "    .social-links {\r\n"
                        + "        margin-top: 25px;\r\n"
                        + "        text-align: center;\r\n"
                        + "    }\r\n"
                        + "    .social-links a {\r\n"
                        + "        display: inline-block;\r\n"
                        + "        margin: 0 10px;\r\n"
                        + "    }\r\n"
                        + "    .social-links img {\r\n"
                        + "        width: 30px;\r\n"
                        + "        height: 30px;\r\n"
                        + "        display: inline-block;\r\n"
                        + "        vertical-align: middle;\r\n"
                        + "    }\r\n"
                        + "    .header-content {\r\n"
                        + "        margin-bottom: 20px;\r\n"
                        + "    }\r\n"
                        + "    .logo {\r\n"
                        + "        width: 100px;\r\n"
                        + "        height: auto;\r\n"
                        + "        margin: 20px 0;\r\n"
                        + "        display: block;\r\n"
                        + "        margin-left: auto;\r\n"
                        + "        margin-right: auto;\r\n"
                        + "    }\r\n"
                        + "</style>\r\n"
                        + "</head>\r\n"
                        + "<body>\r\n"
                        + "<div class=\"container\">\r\n"
                        + "    <div class=\"header-content\">\r\n"
                        + "        <img class=\"logo\" src=\"https://codigos.allticketscol.com/sensorImg.png\" alt=\"Sensor Events Logo\">\r\n"
                        + "    </div>\r\n"
                        + "    <h1>¡Bienvenido a SENSOR EVENTS!</h1>\r\n"
                        + "    <div class=\"content-box\">\r\n"
                        + "        <p>Nos alegra que hayas decidido unirte a nuestra comunidad. En Sensor Events podrás vivir experiencias únicas que estimularán todos tus sentidos.</p>\r\n"
                        + "        <p>Por favor confirma tu registro en el siguiente enlace:</p>\r\n"
                        + "    </div>\r\n"
                        + "    <a class=\"boton\" target=\"_blank\" href=\"https://ticketsensor.com/confirmar-registro/" + idEncriptado + "\">CONFIRMAR REGISTRO</a>\r\n"
                        + "    <div class=\"social-links\">\r\n"
                        + "        <a href=\"https://api.whatsapp.com/send?phone=573202067543\" target=\"_blank\">\r\n"
                        + "            <img src=\"https://cdn-icons-png.flaticon.com/512/3670/3670051.png\" alt=\"WhatsApp\">\r\n"
                        + "        </a>\r\n"
                        + "        <a href=\"https://www.instagram.com/sensor_events/?hl=es-la\" target=\"_blank\">\r\n"
                        + "            <img src=\"https://cdn-icons-png.flaticon.com/512/4138/4138124.png\" alt=\"Instagram\">\r\n"
                        + "        </a>\r\n"
                        + "        <a href=\"https://web.facebook.com/sensorevents?_rdc=1&_rdr\" target=\"_blank\">\r\n"
                        + "            <img src=\"https://cdn-icons-png.flaticon.com/512/5968/5968764.png\" alt=\"Facebook\">\r\n"
                        + "        </a>\r\n"
                        + "    </div>\r\n"
                        + "    <p class=\"footer\">Cualquier duda que tengas, por favor comunícate con nosotros<br><a target=\"_blank\" href=\"https://api.whatsapp.com/send?phone=573202067543&text=Hola%20Sensor%20Events,%20me%20comunico%20porque:\">+57 320 206 7543</a>.</p>\r\n"
                        + "</div>\r\n"
                        + "</body>\r\n"
                        + "</html>\r\n"
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
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY, "text/html; charset=utf-8");

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            System.out.println("Sending...");
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            transport.close();
        }
    }
}