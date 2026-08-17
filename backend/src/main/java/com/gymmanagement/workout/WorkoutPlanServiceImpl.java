package com.gymmanagement.workout;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.coach.CoachService;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.notification.NotificationService;
import com.gymmanagement.notification.NotificationType;
import com.gymmanagement.notification.whatsapp.WhatsAppNotificationService;
import com.gymmanagement.user.UserService;
import com.gymmanagement.workout.dto.WorkoutPlanDetailRequest;
import com.gymmanagement.workout.dto.WorkoutPlanRequest;
import com.gymmanagement.workout.dto.WorkoutPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final ClientService clientService;
    private final CoachService coachService;
    private final ExerciseService exerciseService;
    private final UserService userService;
    private final WorkoutPlanMapper workoutPlanMapper;
    private final NotificationService notificationService;
    private final WhatsAppNotificationService whatsAppNotificationService;

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(String coachEmail, WorkoutPlanRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        assertCoachOwnsClient(coach, client);

        WorkoutPlan plan = WorkoutPlan.builder()
                .client(client)
                .coach(coach)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        plan.setDetails(buildDetails(plan, request.getDetails()));

        WorkoutPlan saved = workoutPlanRepository.save(plan);
        notificationService.createNotification(client.getUser(), NotificationType.WORKOUT_ASSIGNED,
                "A new workout plan '" + saved.getTitle() + "' has been assigned to you");
        whatsAppNotificationService.sendWorkoutPlanAssigned(client, saved.getTitle(), coach.getUser().getFullName());
        return toFullResponse(saved);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(String coachEmail, Long planId, WorkoutPlanRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        WorkoutPlan plan = getPlanEntity(planId);
        assertCoachOwnsPlan(coach, plan);

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.getDetails().clear();
        plan.getDetails().addAll(buildDetails(plan, request.getDetails()));

        WorkoutPlan saved = workoutPlanRepository.save(plan);
        return toFullResponse(saved);
    }

    @Override
    @Transactional
    public void deleteWorkoutPlan(String coachEmail, Long planId) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        WorkoutPlan plan = getPlanEntity(planId);
        assertCoachOwnsPlan(coach, plan);
        workoutPlanRepository.delete(plan);
    }

    @Override
    public WorkoutPlanResponse getWorkoutPlanById(Long planId) {
        return toFullResponse(getPlanEntity(planId));
    }

    @Override
    public List<WorkoutPlanResponse> getPlansByClient(Long clientId) {
        return workoutPlanRepository.findByClientId(clientId).stream()
                .map(this::toFullResponse)
                .toList();
    }

    @Override
    public List<WorkoutPlanResponse> getPlansByCoach(String coachEmail) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        return workoutPlanRepository.findByCoachId(coach.getId()).stream()
                .map(this::toFullResponse)
                .toList();
    }

    @Override
    public WorkoutPlanResponse getTodaysWorkoutForClient(Long clientId) {
        List<WorkoutPlan> plans = workoutPlanRepository.findByClientId(clientId);
        java.time.DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        for (WorkoutPlan plan : plans) {
            boolean hasToday = plan.getDetails().stream().anyMatch(d -> d.getDayOfWeek() == today);
            if (hasToday) {
                return toFullResponse(plan);
            }
        }
        return null;
    }

    private List<WorkoutPlanDetail> buildDetails(WorkoutPlan plan, List<WorkoutPlanDetailRequest> requests) {
        List<WorkoutPlanDetail> details = new ArrayList<>();
        for (WorkoutPlanDetailRequest req : requests) {
            details.add(WorkoutPlanDetail.builder()
                    .workoutPlan(plan)
                    .dayOfWeek(req.getDayOfWeek())
                    .exercise(exerciseService.getExerciseEntityById(req.getExerciseId()))
                    .sets(req.getSets())
                    .reps(req.getReps())
                    .durationMinutes(req.getDurationMinutes())
                    .restTimeSeconds(req.getRestTimeSeconds())
                    .notes(req.getNotes())
                    .build());
        }
        return details;
    }

    private WorkoutPlanResponse toFullResponse(WorkoutPlan plan) {
        WorkoutPlanResponse response = workoutPlanMapper.toResponse(plan);
        response.setDetails(workoutPlanMapper.toDetailResponseList(plan.getDetails()));
        return response;
    }

    private WorkoutPlan getPlanEntity(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout Plan", "id", id));
    }

    private void assertCoachOwnsClient(FitnessCoach coach, Client client) {
        if (client.getAssignedCoach() == null || !client.getAssignedCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
    }

    private void assertCoachOwnsPlan(FitnessCoach coach, WorkoutPlan plan) {
        if (!plan.getCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("You are not authorized to modify this workout plan");
        }
    }
}
