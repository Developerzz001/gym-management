package com.gymmanagement.diet;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianService;
import com.gymmanagement.diet.dto.DietPlanDetailRequest;
import com.gymmanagement.diet.dto.DietPlanRequest;
import com.gymmanagement.diet.dto.DietPlanResponse;
import com.gymmanagement.notification.NotificationService;
import com.gymmanagement.notification.NotificationType;
import com.gymmanagement.notification.whatsapp.WhatsAppNotificationService;
import com.gymmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DietPlanServiceImpl implements DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final ClientService clientService;
    private final DieticianService dieticianService;
    private final UserService userService;
    private final DietPlanMapper dietPlanMapper;
    private final NotificationService notificationService;
    private final WhatsAppNotificationService whatsAppNotificationService;

    @Override
    @Transactional
    public DietPlanResponse createDietPlan(String dieticianEmail, DietPlanRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        assertDieticianOwnsClient(dietician, client);

        DietPlan plan = DietPlan.builder()
                .client(client)
                .dietician(dietician)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        plan.setDetails(buildDetails(plan, request.getDetails()));

        DietPlan saved = dietPlanRepository.save(plan);
        notificationService.createNotification(client.getUser(), NotificationType.DIET_UPDATED,
                "A new diet plan '" + saved.getTitle() + "' has been assigned to you");
        whatsAppNotificationService.sendDietPlanAssigned(client, saved.getTitle(), dietician.getUser().getFullName());
        return toFullResponse(saved);
    }

    @Override
    @Transactional
    public DietPlanResponse updateDietPlan(String dieticianEmail, Long planId, DietPlanRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        DietPlan plan = getPlanEntity(planId);
        assertDieticianOwnsPlan(dietician, plan);

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.getDetails().clear();
        plan.getDetails().addAll(buildDetails(plan, request.getDetails()));

        return toFullResponse(dietPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public void deleteDietPlan(String dieticianEmail, Long planId) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        DietPlan plan = getPlanEntity(planId);
        assertDieticianOwnsPlan(dietician, plan);
        dietPlanRepository.delete(plan);
    }

    @Override
    public DietPlanResponse getDietPlanById(Long planId) {
        return toFullResponse(getPlanEntity(planId));
    }

    @Override
    public List<DietPlanResponse> getPlansByClient(Long clientId) {
        return dietPlanRepository.findByClientId(clientId).stream()
                .map(this::toFullResponse)
                .toList();
    }

    @Override
    public List<DietPlanResponse> getPlansByDietician(String dieticianEmail) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        return dietPlanRepository.findByDieticianId(dietician.getId()).stream()
                .map(this::toFullResponse)
                .toList();
    }

    private List<DietPlanDetail> buildDetails(DietPlan plan, List<DietPlanDetailRequest> requests) {
        List<DietPlanDetail> details = new ArrayList<>();
        for (DietPlanDetailRequest req : requests) {
            details.add(DietPlanDetail.builder()
                    .dietPlan(plan)
                    .mealType(req.getMealType())
                    .foodItem(req.getFoodItem())
                    .quantity(req.getQuantity())
                    .calories(req.getCalories())
                    .mealTime(req.getMealTime())
                    .build());
        }
        return details;
    }

    private DietPlanResponse toFullResponse(DietPlan plan) {
        DietPlanResponse response = dietPlanMapper.toResponse(plan);
        response.setDetails(dietPlanMapper.toDetailResponseList(plan.getDetails()));
        return response;
    }

    private DietPlan getPlanEntity(Long id) {
        return dietPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diet Plan", "id", id));
    }

    private void assertDieticianOwnsClient(Dietician dietician, Client client) {
        if (client.getAssignedDietician() == null || !client.getAssignedDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
    }

    private void assertDieticianOwnsPlan(Dietician dietician, DietPlan plan) {
        if (!plan.getDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("You are not authorized to modify this diet plan");
        }
    }
}
