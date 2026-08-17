package com.gymmanagement.supplement;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianService;
import com.gymmanagement.supplement.dto.SupplementRequest;
import com.gymmanagement.supplement.dto.SupplementResponse;
import com.gymmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementServiceImpl implements SupplementService {

    private final SupplementRepository supplementRepository;
    private final ClientService clientService;
    private final DieticianService dieticianService;
    private final UserService userService;
    private final SupplementMapper supplementMapper;

    @Override
    @Transactional
    public SupplementResponse createSupplement(String dieticianEmail, SupplementRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        assertOwnsClient(dietician, client);

        Supplement supplement = Supplement.builder()
                .client(client)
                .dietician(dietician)
                .name(request.getName())
                .dosage(request.getDosage())
                .timing(request.getTiming())
                .instructions(request.getInstructions())
                .build();
        return supplementMapper.toResponse(supplementRepository.save(supplement));
    }

    @Override
    @Transactional
    public SupplementResponse updateSupplement(String dieticianEmail, Long id, SupplementRequest request) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Supplement supplement = getEntity(id);
        assertOwnsSupplement(dietician, supplement);

        supplement.setName(request.getName());
        supplement.setDosage(request.getDosage());
        supplement.setTiming(request.getTiming());
        supplement.setInstructions(request.getInstructions());
        return supplementMapper.toResponse(supplementRepository.save(supplement));
    }

    @Override
    @Transactional
    public void deleteSupplement(String dieticianEmail, Long id) {
        Dietician dietician = dieticianService.getDieticianEntityByUserId(userService.getUserEntityByEmail(dieticianEmail).getId());
        Supplement supplement = getEntity(id);
        assertOwnsSupplement(dietician, supplement);
        supplementRepository.delete(supplement);
    }

    @Override
    public List<SupplementResponse> getByClient(Long clientId) {
        return supplementRepository.findByClientId(clientId).stream()
                .map(supplementMapper::toResponse)
                .toList();
    }

    private Supplement getEntity(Long id) {
        return supplementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplement", "id", id));
    }

    private void assertOwnsClient(Dietician dietician, Client client) {
        if (client.getAssignedDietician() == null || !client.getAssignedDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
    }

    private void assertOwnsSupplement(Dietician dietician, Supplement supplement) {
        if (!supplement.getDietician().getId().equals(dietician.getId())) {
            throw new BadRequestException("You are not authorized to modify this record");
        }
    }
}
