package com.gymmanagement.notification.whatsapp;

import com.gymmanagement.client.Client;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.diet.MealType;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final WhatsAppMessageSender whatsAppMessageSender;
    private final WhatsAppProperties whatsAppProperties;

    public void sendWorkoutPlanAssigned(Client client, String planTitle, String coachName) {
        sendToUser(client.getUser(),
                "Hi " + client.getUser().getFirstName() + ", your coach " + coachName
                        + " assigned a new workout plan: " + planTitle + ". Open the app to view details.");
    }

    public void sendDietPlanAssigned(Client client, String planTitle, String dieticianName) {
        sendToUser(client.getUser(),
                "Hi " + client.getUser().getFirstName() + ", your dietician " + dieticianName
                        + " assigned a new diet plan: " + planTitle + ". Open the app to view details.");
    }

    public void sendDietReminder(Client client, MealType mealType, LocalTime mealTime, String foodItem, String quantity) {
        String quantityText = quantity == null || quantity.isBlank() ? "" : " (" + quantity + ")";
        sendToUser(client.getUser(),
                "Diet reminder: " + formatMealType(mealType) + " at " + mealTime.format(TIME_FORMATTER)
                        + " - " + foodItem + quantityText + ". Stay consistent.");
    }

    public void sendCoachMissingPlanReminder(FitnessCoach coach, int missingClientCount) {
        sendToUser(coach.getUser(),
                "Reminder: " + missingClientCount + " assigned client(s) still do not have a workout plan."
                        + " Please add plans in Gym Management.");
    }

    public void sendDieticianMissingPlanReminder(Dietician dietician, int missingClientCount) {
        sendToUser(dietician.getUser(),
                "Reminder: " + missingClientCount + " assigned client(s) still do not have a diet plan."
                        + " Please add plans in Gym Management.");
    }

    private void sendToUser(User user, String message) {
        normalizePhone(user.getMobileNumber())
                .ifPresentOrElse(
                        phone -> {
                            boolean sent = whatsAppMessageSender.sendMessage(phone, message);
                            if (!sent) {
                                log.warn("WhatsApp send returned false for userId={} phone={}", user.getId(), phone);
                            }
                        },
                        () -> log.warn("User {} has no valid mobile number for WhatsApp", user.getEmail())
                );
    }

    private Optional<String> normalizePhone(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isBlank()) {
            return Optional.empty();
        }

        String cleaned = mobileNumber.replaceAll("[^0-9+]", "").trim();
        if (cleaned.isBlank()) {
            return Optional.empty();
        }

        if (cleaned.startsWith("00")) {
            cleaned = "+" + cleaned.substring(2);
        }

        if (!cleaned.startsWith("+")) {
            if (cleaned.length() < 10) {
                return Optional.empty();
            }
            cleaned = whatsAppProperties.getDefaultCountryCode() + cleaned;
        }

        if (cleaned.length() < 11) {
            return Optional.empty();
        }

        return Optional.of(cleaned);
    }

    private String formatMealType(MealType mealType) {
        return mealType.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
    }
}
