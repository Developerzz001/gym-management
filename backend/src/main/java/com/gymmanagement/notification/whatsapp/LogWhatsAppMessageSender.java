package com.gymmanagement.notification.whatsapp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogWhatsAppMessageSender implements WhatsAppMessageSender {

    @Override
    public boolean sendMessage(String toPhoneE164, String message) {
        log.info("[WhatsApp:LOG] message queued for phone ending in {}", lastFourDigits(toPhoneE164));
        return true;
    }

    private String lastFourDigits(String phoneNumber) {
        return phoneNumber.length() <= 4 ? phoneNumber : phoneNumber.substring(phoneNumber.length() - 4);
    }
}
