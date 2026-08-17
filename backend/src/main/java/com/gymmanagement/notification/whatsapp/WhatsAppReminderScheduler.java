package com.gymmanagement.notification.whatsapp;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.coach.FitnessCoachRepository;
import com.gymmanagement.diet.DietPlan;
import com.gymmanagement.diet.DietPlanDetail;
import com.gymmanagement.diet.DietPlanRepository;
import com.gymmanagement.diet.MealType;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianRepository;
import com.gymmanagement.workout.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppReminderScheduler {

    private final ClientRepository clientRepository;
    private final DietPlanRepository dietPlanRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final FitnessCoachRepository fitnessCoachRepository;
    private final DieticianRepository dieticianRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;

    @Value("${app.notifications.time-zone:Asia/Kolkata}")
    private String notificationTimeZone;

    @Scheduled(cron = "${app.notifications.diet-reminder-cron:0 * * * * *}")
    @Transactional(readOnly = true)
    public void sendDietMealReminders() {
        LocalTime now = LocalTime.now(ZoneId.of(notificationTimeZone)).withSecond(0).withNano(0);

        for (Client client : clientRepository.findByAssignedDieticianIsNotNull()) {
            DietPlan plan = dietPlanRepository.findTopByClientIdOrderByCreatedAtDesc(client.getId()).orElse(null);
            if (plan == null) {
                continue;
            }

            for (DietPlanDetail detail : plan.getDetails()) {
                LocalTime mealTime = detail.getMealTime() != null ? detail.getMealTime() : defaultMealTime(detail.getMealType());
                if (mealTime != null && mealTime.getHour() == now.getHour() && mealTime.getMinute() == now.getMinute()) {
                    whatsAppNotificationService.sendDietReminder(
                            client,
                            detail.getMealType(),
                            mealTime,
                            detail.getFoodItem(),
                            detail.getQuantity()
                    );
                }
            }
        }
    }

    @Scheduled(cron = "${app.notifications.coach-plan-reminder-cron:0 0 8 * * *}")
    @Transactional(readOnly = true)
    public void remindCoachesToAddWorkoutPlans() {
        for (FitnessCoach coach : fitnessCoachRepository.findAll()) {
            List<Client> assignedClients = clientRepository.findByAssignedCoachId(coach.getId());
            int missingPlans = (int) assignedClients.stream()
                    .filter(client -> !workoutPlanRepository.existsByClientIdAndCoachId(client.getId(), coach.getId()))
                    .count();

            if (missingPlans > 0) {
                whatsAppNotificationService.sendCoachMissingPlanReminder(coach, missingPlans);
            }
        }
    }

    @Scheduled(cron = "${app.notifications.dietician-plan-reminder-cron:0 15 8 * * *}")
    @Transactional(readOnly = true)
    public void remindDieticiansToAddDietPlans() {
        for (Dietician dietician : dieticianRepository.findAll()) {
            List<Client> assignedClients = clientRepository.findByAssignedDieticianId(dietician.getId());
            int missingPlans = (int) assignedClients.stream()
                    .filter(client -> !dietPlanRepository.existsByClientIdAndDieticianId(client.getId(), dietician.getId()))
                    .count();

            if (missingPlans > 0) {
                whatsAppNotificationService.sendDieticianMissingPlanReminder(dietician, missingPlans);
            }
        }
    }

    private LocalTime defaultMealTime(MealType mealType) {
        return switch (mealType) {
            case BREAKFAST -> LocalTime.of(9, 0);
            case MORNING_SNACK -> LocalTime.of(11, 0);
            case LUNCH -> LocalTime.of(14, 0);
            case EVENING_SNACK -> LocalTime.of(17, 0);
            case DINNER -> LocalTime.of(20, 0);
        };
    }
}
