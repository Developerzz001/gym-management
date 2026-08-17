package com.gymmanagement.coach;

import com.gymmanagement.coach.dto.CoachRequest;
import com.gymmanagement.coach.dto.CoachResponse;
import com.gymmanagement.common.dto.PageResponse;

public interface CoachService {

    CoachResponse createCoach(CoachRequest request);

    CoachResponse updateCoach(Long id, CoachRequest request);

    void deleteCoach(Long id);

    CoachResponse getCoachById(Long id);

    PageResponse<CoachResponse> getCoaches(String keyword, int page, int size);

    FitnessCoach getCoachEntityById(Long id);

    FitnessCoach getCoachEntityByUserId(Long userId);
}
