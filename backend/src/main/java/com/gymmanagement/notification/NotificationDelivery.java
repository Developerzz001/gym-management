package com.gymmanagement.notification;

import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "notification_deliveries", uniqueConstraints =
        @UniqueConstraint(name = "uk_notification_event_channel", columnNames = {"event_key", "channel"}),
        indexes = @Index(name = "idx_notification_delivery_recipient", columnList = "recipient_id,created_at"))
public class NotificationDelivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "event_key", nullable = false, length = 160)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;
}