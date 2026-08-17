package com.gymmanagement.workout;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.coach.CoachService;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.notification.NotificationService;
import com.gymmanagement.notification.NotificationType;
import com.gymmanagement.user.UserService;
import com.gymmanagement.workout.dto.SessionRequest;
import com.gymmanagement.workout.dto.SessionResponse;
import com.gymmanagement.workout.dto.SessionStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionServiceImpl implements SessionService {

    private final TrainingSessionRepository sessionRepository;
    private final ClientService clientService;
    private final CoachService coachService;
    private final UserService userService;
    private final SessionMapper sessionMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SessionResponse scheduleSession(String coachEmail, SessionRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        if (client.getAssignedCoach() == null || !client.getAssignedCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
        TrainingSession session = TrainingSession.builder()
                .client(client)
                .coach(coach)
                .sessionDateTime(request.getSessionDateTime())
                .status(SessionStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();
        TrainingSession saved = sessionRepository.save(session);
        notificationService.createNotification(client.getUser(), NotificationType.SESSION_SCHEDULED,
                "A personal training session has been scheduled on " + saved.getSessionDateTime());
        return sessionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse updateSessionStatus(String coachEmail, Long sessionId, SessionStatusUpdateRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training Session", "id", sessionId));
        if (!session.getCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("You are not authorized to update this session");
        }
        session.setStatus(request.getStatus());
        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    @Override
    public List<SessionResponse> getSessionsByClient(Long clientId) {
        return sessionRepository.findByClientIdOrderBySessionDateTimeDesc(clientId).stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    @Override
    public List<SessionResponse> getSessionsByCoach(String coachEmail) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        return sessionRepository.findByCoachIdOrderBySessionDateTimeDesc(coach.getId()).stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    @Override
    public List<SessionResponse> getUpcomingSessionsForClient(Long clientId) {
        return sessionRepository.findByClientIdAndStatusOrderBySessionDateTimeAsc(clientId, SessionStatus.SCHEDULED).stream()
                .map(sessionMapper::toResponse)
                .toList();
    }
}
