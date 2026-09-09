package com.gymmanagement.dietician;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.dietician.dto.DieticianRequest;
import com.gymmanagement.dietician.dto.DieticianResponse;
import com.gymmanagement.dietician.dto.DieticianProfileRequest;

public interface DieticianService {

    DieticianResponse createDietician(DieticianRequest request);

    DieticianResponse updateDietician(Long id, DieticianRequest request);

    DieticianResponse activateDietician(Long id);

    DieticianResponse deactivateDietician(Long id);

    void deleteDietician(Long id);

    DieticianResponse getDieticianById(Long id);

    DieticianResponse getDieticianByUserId(Long userId);

    DieticianResponse updateOwnProfile(Long userId, DieticianProfileRequest request);

    PageResponse<DieticianResponse> getDieticians(String keyword, int page, int size);

    Dietician getDieticianEntityById(Long id);

    Dietician getDieticianEntityByUserId(Long userId);
}
