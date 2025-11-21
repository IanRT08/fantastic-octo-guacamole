package mx.edu.utez.fantasticoctoguacamole.servicios;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import mx.edu.utez.fantasticoctoguacamole.modelo.GestorTokens;

import java.util.Properties;

public class ServicioEmail {

    public boolean enviarTokenRecuperacion(String email, String token) {
        try {
            String asunto = "Recuperación de Contraseña - ElectroStock";
            String mensaje = "Hola,\n\n" +
                    "Has solicitado recuperar tu contraseña.\n" +
                    "Tu token de verificación es: " + token + "\n\n" +
                    "Este token expirará en 15 minutos.\n\n" +
                    "Si no solicitaste este cambio, ignora este mensaje.\n\n" +
                    "Saludos,\nEquipo ElectroStock";
            Properties props = new Properties();
            final String remitente = "20243ds030@utez.edu.mx"; //Correo
            final String clave = "qspp gzog sgci escr"; //clave de aplicación
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, clave);
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@electrostock.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(asunto);
            message.setText(mensaje);
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
            return false;
        }
    }
}
