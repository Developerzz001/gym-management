package com.gymmanagement.notification.whatsapp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogWhatsAppMessageSender implements WhatsAppMessageSender {

    @Override
    public boolean sendMessage(String toPhoneE164, String message) {
        log.info("[WhatsApp:LOG] to={} message={}", toPhoneE164, message);
        return true;
    }
}
