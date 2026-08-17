package com.gymmanagement.medicine;

import com.gymmanagement.medicine.dto.MedicineRequest;
import com.gymmanagement.medicine.dto.MedicineResponse;

import java.util.List;

public interface MedicineService {

    MedicineResponse createMedicine(String dieticianEmail, MedicineRequest request);

    MedicineResponse updateMedicine(String dieticianEmail, Long id, MedicineRequest request);

    void deleteMedicine(String dieticianEmail, Long id);

    List<MedicineResponse> getByClient(Long clientId);
}
