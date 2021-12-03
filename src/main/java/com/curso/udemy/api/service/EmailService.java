package com.curso.udemy.api.service;

import java.util.List;

public interface EmailService {
    void sendMails(String message, List<String> mailList);
}
