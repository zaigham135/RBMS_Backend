package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendTaskUpdateEmail(String to, String subject, String body) {
        log.debug("Sending email: to={}, subject={}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Task Management System <" + fromEmail + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Email failed: to={}, subject={}, error={}", to, subject, e.getMessage());
        }
    }
}
