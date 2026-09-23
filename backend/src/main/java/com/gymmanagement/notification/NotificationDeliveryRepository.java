package com.gymmanagement.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
    boolean existsByEventKeyAndChannel(String eventKey, NotificationChannel channel);
}