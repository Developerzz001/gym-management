package com.gymmanagement.workout;

import com.gymmanagement.workout.dto.SessionRequest;
import com.gymmanagement.workout.dto.SessionResponse;
import com.gymmanagement.workout.dto.SessionStatusUpdateRequest;

import java.util.List;

public interface SessionService {

    SessionResponse scheduleSession(String coachEmail, SessionRequest request);

    SessionResponse updateSessionStatus(String coachEmail, Long sessionId, SessionStatusUpdateRequest request);

    List<SessionResponse> getSessionsByClient(Long clientId);

    List<SessionResponse> getSessionsByCoach(String coachEmail);

    List<SessionResponse> getUpcomingSessionsForClient(Long clientId);
}
