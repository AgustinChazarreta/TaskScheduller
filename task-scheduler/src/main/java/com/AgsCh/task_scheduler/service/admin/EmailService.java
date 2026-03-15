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
            helper.setFrom("agustinchazarreta00@gmail.com", "Agustin Chazarreta");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true); // true = HTML

        helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));

        mailSender.send(message);
    }
}