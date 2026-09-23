package com.gymmanagement.notification;

import com.gymmanagement.notification.whatsapp.WhatsAppNotificationService;
import com.gymmanagement.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomatedNotificationService {

    private final NotificationService notificationService;
    private final NotificationDeliveryRepository deliveryRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final JavaMailSender mailSender;

    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;
    @Value("${app.notifications.from-email:noreply@gym.local}")
    private String fromEmail;

    @Transactional
    public void send(User recipient, NotificationType type, String subject, String message, String eventKey) {
        deliver(recipient, eventKey, NotificationChannel.IN_APP,
                () -> notificationService.createNotification(recipient, type, message));
        if (emailEnabled && recipient.getEmail() != null && !recipient.getEmail().isBlank()) {
            deliver(recipient, eventKey, NotificationChannel.EMAIL, () -> sendEmail(recipient, subject, message));
        }
        deliver(recipient, eventKey, NotificationChannel.WHATSAPP,
                () -> whatsAppNotificationService.sendToUser(recipient, message));
    }

    private void deliver(User recipient, String eventKey, NotificationChannel channel, Runnable action) {
        if (deliveryRepository.existsByEventKeyAndChannel(eventKey, channel)) {
            return;
        }
        try {
            action.run();
            deliveryRepository.save(NotificationDelivery.builder()
                    .recipient(recipient).eventKey(eventKey).channel(channel).build());
        } catch (Exception exception) {
            log.error("Notification delivery failed: eventKey={} channel={}", eventKey, channel, exception);
        }
    }

    private void sendEmail(User recipient, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(recipient.getEmail());
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
}