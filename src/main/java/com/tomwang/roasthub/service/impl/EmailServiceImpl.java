package com.tomwang.roasthub.service.impl;

import com.tomwang.roasthub.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an mail.properties.
     * @param to Email address of the recipient.
     * @param subject Subject of the mail.properties.
     * @param text Body of the mail.properties.
     */
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("guanjie.wang@qq.com"); // Replace with your mail.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
