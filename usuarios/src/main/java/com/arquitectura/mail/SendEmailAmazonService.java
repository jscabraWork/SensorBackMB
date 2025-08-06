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

	
	
    // Replace sender@example.com with your "From" address.
    // This address must be verified.
    static final String FROM = "noreply_tickets@allticketscol.com";
    static final String FROMNAME = "AllTickets_NO_RESPONDER";
	
    
    // Replace smtp_username with your Amazon SES SMTP user name.
    static final String SMTP_USERNAME = "AKIAZ4YTFVLRGWUACVUD";
    
    // Replace smtp_password with your Amazon SES SMTP password.
    static final String SMTP_PASSWORD = "BPNnz1m2Tu7EMWoC7t0iDH7ap8cefA3OiFhtODMhbKtV ";
    
    // The name of the Configuration Set to use for this message.
    // If you comment out or remove this variable, you will also need to
    // comment out or remove the header below.
    //static final String CONFIGSET = "ConfigSet";
    
    // Amazon SES SMTP host name. This example uses the EE.UU. Oeste (Oregón) region.
    // See https://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html#region-endpoints
    // for more information.
    static final String HOST = "email-smtp.us-east-1.amazonaws.com";
    
    // The port you will connect to on the Amazon SES SMTP endpoint. 
    static final int PORT = 587;
    
  
    
    
    
    public void mandarCorreoContrasenia(String to, String idEncriptado) throws Exception {

        
    	String SUBJECT = "Contraseña recuperada";
        
    	 String BODY = String.join(
    	    	    System.getProperty("line.separator"),
    			"<!DOCTYPE html>\r\n"
    			+ "<html lang=\"es\">\r\n"
    			+ "<head>\r\n"
    			+ "<meta charset=\"UTF-8\">\r\n"
    			+ "<title>Bienvenido a All Tickets</title>\r\n"
    			+ "<style>\r\n"
    			+ "    body {\r\n"
    			+ "        font-family: Arial, sans-serif;\r\n"
    			+ "        margin: 0;\r\n"
    			+ "        padding: 0;\r\n"
    			+ "        background: #f4f4f4;\r\n"
    			+ "        text-align: center;\r\n"
    			+ "        color: #333;\r\n"
    			+ "    }\r\n"
    			+ "    .container {\r\n"
    			+ "        width: 100%;\r\n"
    			+ "        max-width: 400px;\r\n"
    			+ "        margin: 50px auto;\r\n"
    			+ "        background: white;\r\n"
    			+ "        padding: 20px;\r\n"
    			+ "        box-shadow: 0 0 10px rgba(0,0,0,0.1);\r\n"
    			+ "    }\r\n"
    			+ "    h1 {\r\n"
    			+ "        font-size: 24px;\r\n"
    			+ "        color: #ed701c;\r\n"
    			+ "    }\r\n"
    			+ "    p {\r\n"
    			+ "        font-size: 14px;\r\n"
    			+ "        color: #666;\r\n"
    			+ "        line-height: 1.4;\r\n"
    			+ "    }\r\n"
    			+ "    .boton {\r\n"
    			+ "        display: inline-block;\r\n"
    			+ "        margin-top: 20px;\r\n"
    			+ "        padding: 10px 20px;\r\n"
    			+ "        background-color: #ed701c;\r\n"
    			+ "        color: white;\r\n"
    			+ "        text-decoration: none;\r\n"
    			+ "        border-radius: 4px;\r\n"
    			+ "    }\r\n"
    			+ "    .boton:hover {\r\n"
    			+ "        background-color: #ed701c;\r\n"
    			+ "    }\r\n"
    			+ "    .footer a{\r\n"
    			+ "       color: #ed701c;\r\n"
    			+ "    }\r\n"
    			+ "    .footer {\r\n"
    			+ "        margin-top: 30px;\r\n"
    			+ "        font-size: 12px;\r\n"
    			+ "        color: #aaa;\r\n"
    			+ "    }\r\n"
    			+ "</style>\r\n"
    			+ "</head>\r\n"
    			+ "<body>\r\n"
    			+ "<div class=\"container\">\r\n"
    			+ "    <img src=\"https://allticketscol.com/assets/images/img/logo2.webp\" alt=\"All TICKETS Logo\" style=\"width: 200px; height: auto; margin: 20px 0;\">\r\n"
    			+ "    <h1>Recuperación de contraseña</h1>\r\n"
    			+ "    <p>Haz solciitado un cambio de contraseña, en caso de que no lo hayas hecho te recomendamos cambiar tu correo de tu usuario</p>\r\n"
    			+ "    <a class=\"boton\"target=\"_blank\"  href=\"https://allticketscol.com/cambio-contrasena/"+idEncriptado+"\">RECUPERAR</a>\r\n"
    			+ "    <p class=\"footer\">Cualquier duda que tengas por favor comunicaté con nuestro número de whatsapp<a target=\"_blank\"  href=\"https://api.whatsapp.com/send?phone=573209644716&text=Hola%20All%20Tickets,%20me%20comunico%20con%20ustedes%C2%A0porque:\">+57 320 9644716</a>.</p>\r\n"
    			+ "    \r\n"
    			+ "</div>\r\n"
    			+ "</body>\r\n"
    			+ "</html>\r\n"
    			+ ""
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
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY,"text/html; charset=utf-8");
        
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
    

    public void mandarCorreoUsuario(String usuario, String idEncriptado, String to) throws Exception {
    	String SUBJECT ="Gracias por unirte a alltickets";
    	
    	 String BODY = String.join(
    	    	    System.getProperty("line.separator"),
    	    	    "<!DOCTYPE html>\r\n"
    	    	    + "<html lang=\"es\">\r\n"
    	    	    + "<head>\r\n"
    	    	    + "<meta charset=\"UTF-8\">\r\n"
    	    	    + "<title>Bienvenido a All Tickets</title>\r\n"
    	    	    + "<style>\r\n"
    	    	    + "    body {\r\n"
    	    	    + "        font-family: Arial, sans-serif;\r\n"
    	    	    + "        margin: 0;\r\n"
    	    	    + "        padding: 0;\r\n"
    	    	    + "        background: #f4f4f4;\r\n"
    	    	    + "        text-align: center;\r\n"
    	    	    + "        color: #333;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    .container {\r\n"
    	    	    + "        width: 100%;\r\n"
    	    	    + "        max-width: 400px;\r\n"
    	    	    + "        margin: 50px auto;\r\n"
    	    	    + "        background: white;\r\n"
    	    	    + "        padding: 20px;\r\n"
    	    	    + "        box-shadow: 0 0 10px rgba(0,0,0,0.1);\r\n"
    	    	    + "    }\r\n"
    	    	    + "    h1 {\r\n"
    	    	    + "        font-size: 24px;\r\n"
    	    	    + "        color: #ed701c;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    p {\r\n"
    	    	    + "        font-size: 14px;\r\n"
    	    	    + "        color: #666;\r\n"
    	    	    + "        line-height: 1.4;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    .boton {\r\n"
    	    	    + "        display: inline-block;\r\n"
    	    	    + "        margin-top: 20px;\r\n"
    	    	    + "        padding: 10px 20px;\r\n"
    	    	    + "        background-color: #ed701c;\r\n"
    	    	    + "        color: white;\r\n"
    	    	    + "        text-decoration: none;\r\n"
    	    	    + "        border-radius: 4px;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    .boton:hover {\r\n"
    	    	    + "        background-color: #ed701c;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    .footer a{\r\n"
    	    	    + "       color: #ed701c;\r\n"
    	    	    + "    }\r\n"
    	    	    + "    .footer {\r\n"
    	    	    + "        margin-top: 30px;\r\n"
    	    	    + "        font-size: 12px;\r\n"
    	    	    + "        color: #aaa;\r\n"
    	    	    + "    }\r\n"
    	    	    + "</style>\r\n"
    	    	    + "</head>\r\n"
    	    	    + "<body>\r\n"
    	    	    + "<div class=\"container\">\r\n"
    	    	    + "    <img src=\"https://allticketscol.com/assets/images/img/logo2.webp\" alt=\"All TICKETS Logo\" style=\"width: 200px; height: auto; margin: 20px 0;\">\r\n"
    	    	    + "    <h1>¡Bienvenido a ALL TICKETS!</h1>\r\n"
    	    	    + "    <p>Nos alegra que disfrutes tus eventos con nosotros. All Tickets tiene eventos exclusivos para ti, por favor confirma tu reserva en el siguiente enlace</p>\r\n"
    	    	    + "    <a class=\"boton\"target=\"_blank\"  href=\"https://allticketscol.com/confirmar-registro/"+idEncriptado+"\">CONFIRMAR</a>\r\n"
    	    	    + "    <p class=\"footer\">Cualquier duda que tengas por favor comunicaté con nuestro número de whatsapp<a target=\"_blank\"  href=\"https://api.whatsapp.com/send?phone=573209644716&text=Hola%20All%20Tickets,%20me%20comunico%20con%20ustedes%C2%A0porque:\">+57 320 9644716</a>.</p>\r\n"
    	    	    + "    \r\n"
    	    	    + "</div>\r\n"
    	    	    + "</body>\r\n"
    	    	    + "</html>\r\n"
    	    	    + ""
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
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY,"text/html; charset=utf-8");
        
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