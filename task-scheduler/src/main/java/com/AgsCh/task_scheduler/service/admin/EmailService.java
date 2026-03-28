package com.AgsCh.task_scheduler.service.admin;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;

import java.io.UnsupportedEncodingException;

import org.springframework.core.io.ByteArrayResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachmentBytes,
            String attachmentName) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // ✅ Esto evita el error 555

        try {
            helper.setFrom("escalasoes@arautos.org.br", "Task Scheduler");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true); // true = HTML

        helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));

        mailSender.send(message);
    }

    public void sendVerificationEmail(String email, String link) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("escalasoes@arautos.org.br", "Task Scheduler");
            helper.setTo(email);
            helper.setSubject("Verificá tu cuenta");

            String html = """
                    <div style="font-family: Arial, sans-serif;">
                        <h2>Verificación de cuenta</h2>
                        <p>Gracias por registrarte como administrador.</p>
                        <p>Hacé click en el botón para verificar tu email:</p>

                        <a href="%s"
                           style="display:inline-block;
                                  padding:10px 20px;
                                  background-color:#0d6efd;
                                  color:white;
                                  text-decoration:none;
                                  border-radius:5px;">
                            Verificar email
                        </a>

                        <p style="margin-top:20px;">Este enlace expira en 24 horas.</p>
                    </div>
                    """.formatted(link);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando email de verificación", e);
        }
    }

    public void sendPasswordResetEmail(String email, String link) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("escalasoes@arautos.org.br", "Task Scheduler");
            helper.setTo(email);
            helper.setSubject("Recuperación de contraseña");

            String html = """
                    <div style="font-family: Arial, sans-serif;">
                        <h2>Recuperación de contraseña</h2>
                        <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                        <p>Hacé click en el botón para continuar:</p>

                        <a href="%s"
                           style="display:inline-block;
                                  padding:10px 20px;
                                  background-color:#dc3545;
                                  color:white;
                                  text-decoration:none;
                                  border-radius:5px;">
                            Restablecer contraseña
                        </a>

                        <p style="margin-top:20px;">Este enlace expira en 30 minutos.</p>

                        <p>Si no solicitaste esto, podés ignorar este mensaje.</p>
                    </div>
                    """.formatted(link);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando email de recuperación", e);
        }
    }

    // método genérico para mails de creación de cuenta
    private void sendCredentialsEmail(String email, String nombre, String password, String rol) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("escalasoes@arautos.org.br", "Task Scheduler");
            helper.setTo(email);
            helper.setSubject("Acceso a Task Scheduler - " + rol);

            String html = """
                    <div style="font-family: Arial, sans-serif; background-color:#f5f7fa; padding:20px;">

                        <div style="max-width:600px; margin:auto; background:white; border-radius:8px; padding:25px; box-shadow:0 2px 8px rgba(0,0,0,0.05);">

                            <h2 style="color:#2c3e50; margin-bottom:10px;">Cuenta creada correctamente</h2>

                            <p style="color:#555;">
                                Hola <strong>%s</strong>,
                            </p>

                            <p style="color:#555;">
                                Se ha creado tu cuenta con rol <strong>%s</strong> en la plataforma <strong>Task Scheduler</strong>.
                            </p>

                            <div style="margin:20px 0; padding:15px; background:#f1f3f5; border-radius:6px;">
                                <p style="margin:5px 0;"><strong>Usuario:</strong> %s</p>
                                <p style="margin:5px 0;"><strong>Contraseña temporal:</strong> %s</p>
                            </div>

                            <p style="color:#555;">
                                Tu cuenta se encuentra actualmente <strong>pendiente de activación</strong>.
                                Recibirás una notificación cuando esté habilitada.
                            </p>

                            <p style="color:#555;">
                                Por motivos de seguridad, deberás cambiar tu contraseña en el primer inicio de sesión.
                            </p>

                            <p style="margin-top:25px; font-size:13px; color:#888;">
                                Si no solicitaste esta cuenta, podés ignorar este mensaje.
                            </p>

                        </div>
                    </div>
                    """
                    .formatted(nombre, rol, email, password);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new BusinessException("Error enviando email de credenciales para " + rol, e);
        }
    }

    // método genérico para mails de activación
    private void sendActivationEmail(String username, String nombre, String rol) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("escalasoes@arautos.org.br", "Task Scheduler");
            helper.setTo(username);
            helper.setSubject("Cuenta activada - Task Scheduler");

            String loginUrl = "https://www.funcoescheznous.org";

            String html = """
                    <div style="font-family: Arial, sans-serif; background-color:#f5f7fa; padding:20px;">

                        <div style="max-width:600px; margin:auto; background:white; border-radius:8px; padding:25px; box-shadow:0 2px 8px rgba(0,0,0,0.05);">

                            <h2 style="color:#2c3e50; margin-bottom:10px;">Cuenta activada</h2>

                            <p style="color:#555;">
                                Hola <strong>%s</strong>,
                            </p>

                            <p style="color:#555;">
                                Tu cuenta con rol <strong>%s</strong> en <strong>Task Scheduler</strong> ha sido activada.
                            </p>

                            <p style="color:#555;">
                                Ahora podés iniciar sesión y comenzar a usar la plataforma haciendo click en el siguiente botón:
                            </p>

                            <a href="%s"
                               style="display:inline-block;
                                      padding:10px 20px;
                                      background-color:#0d6efd;
                                      color:white;
                                      text-decoration:none;
                                      border-radius:5px;
                                      margin-top:10px;">
                                Ir al login
                            </a>

                            <p style="margin-top:25px; font-size:13px; color:#888;">
                                Si no solicitaste esta activación, por favor contactá al administrador.
                            </p>

                        </div>
                    </div>
                    """
                    .formatted(nombre, rol, loginUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new BusinessException("Error enviando email de activación para " + rol, e);
        }
    }

    // Métodos específicos solo llaman al genérico:
    public void sendWebmasterCredentialsEmail(String email, String nombre, String password) {
        sendCredentialsEmail(email, nombre, password, "WEBMASTER");
    }

    public void sendWebmasterActivationEmail(String username, String nombre) {
        sendActivationEmail(username, nombre, "WEBMASTER");
    }

    public void sendAdminCredentialsEmail(String email, String nombre, String password) {
        sendCredentialsEmail(email, nombre, password, "ADMIN");
    }

    public void sendAdminActivationEmail(String username, String nombre) {
        sendActivationEmail(username, nombre, "ADMIN");
    }
}
