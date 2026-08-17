package com.gymmanagement.supplement;

import com.gymmanagement.supplement.dto.SupplementRequest;
import com.gymmanagement.supplement.dto.SupplementResponse;

import java.util.List;

public interface SupplementService {

    SupplementResponse createSupplement(String dieticianEmail, SupplementRequest request);

    SupplementResponse updateSupplement(String dieticianEmail, Long id, SupplementRequest request);

    void deleteSupplement(String dieticianEmail, Long id);

    List<SupplementResponse> getByClient(Long clientId);
}
