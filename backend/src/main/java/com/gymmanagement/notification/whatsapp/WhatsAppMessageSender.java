package com.gymmanagement.notification.whatsapp;

public interface WhatsAppMessageSender {

    boolean sendMessage(String toPhoneE164, String message);
}
