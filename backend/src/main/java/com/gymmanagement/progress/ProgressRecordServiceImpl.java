package com.gymmanagement.progress;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.coach.CoachService;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.progress.dto.ProgressRecordRequest;
import com.gymmanagement.progress.dto.ProgressRecordResponse;
import com.gymmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressRecordServiceImpl implements ProgressRecordService {

    private final ProgressRecordRepository progressRecordRepository;
    private final ClientService clientService;
    private final CoachService coachService;
    private final UserService userService;
    private final ProgressRecordMapper progressRecordMapper;

    @Override
    @Transactional
    public ProgressRecordResponse addProgress(String coachEmail, ProgressRecordRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        Client client = clientService.getClientEntityById(request.getClientId());
        assertCoachOwnsClient(coach, client);

        ProgressRecord record = ProgressRecord.builder()
                .client(client)
                .recordedByCoach(coach)
                .recordDate(request.getRecordDate())
                .weightKg(request.getWeightKg())
                .bmi(calculateBmi(request.getWeightKg(), client.getHeightCm()))
                .chestCm(request.getChestCm())
                .waistCm(request.getWaistCm())
                .armsCm(request.getArmsCm())
                .shoulderCm(request.getShoulderCm())
                .thighCm(request.getThighCm())
                .build();
        return progressRecordMapper.toResponse(progressRecordRepository.save(record));
    }

    @Override
    @Transactional
    public ProgressRecordResponse updateProgress(String coachEmail, Long id, ProgressRecordRequest request) {
        FitnessCoach coach = coachService.getCoachEntityByUserId(userService.getUserEntityByEmail(coachEmail).getId());
        ProgressRecord record = progressRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress Record", "id", id));
        if (record.getRecordedByCoach() != null && !record.getRecordedByCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("You are not authorized to modify this progress record");
        }
        record.setRecordDate(request.getRecordDate());
        record.setWeightKg(request.getWeightKg());
        record.setBmi(calculateBmi(request.getWeightKg(), record.getClient().getHeightCm()));
        record.setChestCm(request.getChestCm());
        record.setWaistCm(request.getWaistCm());
        record.setArmsCm(request.getArmsCm());
        record.setShoulderCm(request.getShoulderCm());
        record.setThighCm(request.getThighCm());
        return progressRecordMapper.toResponse(progressRecordRepository.save(record));
    }

    @Override
    public List<ProgressRecordResponse> getByClient(Long clientId) {
        return progressRecordRepository.findByClientIdOrderByRecordDateDesc(clientId).stream()
                .map(progressRecordMapper::toResponse)
                .toList();
    }

    private Double calculateBmi(Double weightKg, Double heightCm) {
        if (weightKg == null || heightCm == null || heightCm <= 0) {
            return null;
        }
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    private void assertCoachOwnsClient(FitnessCoach coach, Client client) {
        if (client.getAssignedCoach() == null || !client.getAssignedCoach().getId().equals(coach.getId())) {
            throw new BadRequestException("This client is not assigned to you");
        }
    }
}
