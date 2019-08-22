package me.exrates.externalservice.services;

import me.exrates.externalservice.entities.enums.EmailType;

import java.util.Map;

public interface MailSenderService {

    void send(EmailType type, String to, Map<String, Object> properties);

    void send(EmailType type, String title, String to, Map<String, Object> properties);
}