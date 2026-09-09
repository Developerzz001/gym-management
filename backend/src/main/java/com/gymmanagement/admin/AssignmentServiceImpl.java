package com.gymmanagement.admin;

import com.gymmanagement.admin.dto.AssignCoachRequest;
import com.gymmanagement.admin.dto.AssignDieticianRequest;
import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientMapper;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.client.RegistrationType;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.coach.CoachService;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianService;
import com.gymmanagement.notification.NotificationService;
import com.gymmanagement.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final ClientService clientService;
    private final CoachService coachService;
    private final DieticianService dieticianService;
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    @Override
    public ClientResponse assignCoach(AssignCoachRequest request) {
        Client client = clientService.getClientEntityById(request.getClientId());
        if (client.getRegistrationType() == RegistrationType.INQUIRY) {
            throw new IllegalArgumentException("An inquiry must be registered as a client before assignment");
        }
        FitnessCoach coach = coachService.getCoachEntityById(request.getCoachId());
        client.setAssignedCoach(coach);
        client = clientRepository.save(client);
        notificationService.createNotification(client.getUser(), NotificationType.WORKOUT_ASSIGNED,
                "You have been assigned a new fitness coach: " + coach.getUser().getFullName());
        return clientMapper.toResponse(client);
    }

    @Override
    public ClientResponse assignDietician(AssignDieticianRequest request) {
        Client client = clientService.getClientEntityById(request.getClientId());
        if (client.getRegistrationType() == RegistrationType.INQUIRY) {
            throw new IllegalArgumentException("An inquiry must be registered as a client before assignment");
        }
        Dietician dietician = dieticianService.getDieticianEntityById(request.getDieticianId());
        client.setAssignedDietician(dietician);
        client = clientRepository.save(client);
        notificationService.createNotification(client.getUser(), NotificationType.DIET_UPDATED,
                "You have been assigned a new dietician: " + dietician.getUser().getFullName());
        return clientMapper.toResponse(client);
    }
}
