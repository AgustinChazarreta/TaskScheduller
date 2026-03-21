package com.AgsCh.task_scheduler.service.admin;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
}
