package com.gymmanagement.coach;

import com.gymmanagement.coach.dto.CoachRequest;
import com.gymmanagement.coach.dto.CoachResponse;
import com.gymmanagement.coach.dto.CoachProfileRequest;
import com.gymmanagement.common.dto.PageResponse;

public interface CoachService {

    CoachResponse createCoach(CoachRequest request);

    CoachResponse updateCoach(Long id, CoachRequest request);

    CoachResponse activateCoach(Long id);

    CoachResponse deactivateCoach(Long id);

    void deleteCoach(Long id);

    CoachResponse getCoachById(Long id);

    CoachResponse getCoachByUserId(Long userId);

    CoachResponse updateOwnProfile(Long userId, CoachProfileRequest request);

    PageResponse<CoachResponse> getCoaches(String keyword, int page, int size);

    FitnessCoach getCoachEntityById(Long id);

    FitnessCoach getCoachEntityByUserId(Long userId);
}
