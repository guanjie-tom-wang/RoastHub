package com.tomwang.roasthub.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
