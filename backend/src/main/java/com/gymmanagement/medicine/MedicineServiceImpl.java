package com.gymmanagement.medicine;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianService;
import com.gymmanagement.medicine.dto.MedicineRequest;
import com.gymmanagement.medicine.dto.MedicineResponse;
import com.gymmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final ClientService clientService;
    private final DieticianService dieticianService;
    private final UserService userService;
    private final MedicineMapper medicineMapper;

    @Override
    @Transactional
    public MedicineResponse createMedicine(String dieticianEmail, MedicineRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        assertOwnsClient(dietician, client);

        Medicine medicine = Medicine.builder()
                .client(client)
                .dietician(dietician)
                .name(request.getName())
                .dosage(request.getDosage())
                .timing(request.getTiming())
                .instructions(request.getInstructions())
                .build();
        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Override
    @Transactional
    public MedicineResponse updateMedicine(String dieticianEmail, Long id, MedicineRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Medicine medicine = getEntity(id);
        assertOwnsMedicine(dietician, medicine);

        medicine.setName(request.getName());
        medicine.setDosage(request.getDosage());
        medicine.setTiming(request.getTiming());
        medicine.setInstructions(request.getInstructions());
        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Override
    @Transactional
    public void deleteMedicine(String dieticianEmail, Long id) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Medicine medicine = getEntity(id);
        assertOwnsMedicine(dietician, medicine);
        medicineRepository.delete(medicine);
    }

    @Override
    public List<MedicineResponse> getByClient(Long clientId) {
        return medicineRepository.findByClientId(clientId).stream()
                .map(medicineMapper::toResponse)
                .toList();
    }

    private Medicine getEntity(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
    }

    private void assertOwnsClient(Dietician dietician, Client client) {
        if (client.getAssignedDietician() == null || !client.getAssignedDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
    }

    private void assertOwnsMedicine(Dietician dietician, Medicine medicine) {
        if (!medicine.getDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("You are not authorized to modify this record");
        }
    }
}
