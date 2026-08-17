package com.gymmanagement.dietician;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.dietician.dto.DieticianRequest;
import com.gymmanagement.dietician.dto.DieticianResponse;

public interface DieticianService {

    DieticianResponse createDietician(DieticianRequest request);

    DieticianResponse updateDietician(Long id, DieticianRequest request);

    void deleteDietician(Long id);

    DieticianResponse getDieticianById(Long id);

    PageResponse<DieticianResponse> getDieticians(String keyword, int page, int size);

    Dietician getDieticianEntityById(Long id);

    Dietician getDieticianEntityByUserId(Long userId);
}
