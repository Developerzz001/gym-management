package com.gymmanagement.coach;

import com.gymmanagement.coach.dto.CoachRequest;
import com.gymmanagement.coach.dto.CoachResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoachServiceImpl implements CoachService {

    private final FitnessCoachRepository coachRepository;
    private final UserRepository userRepository;
    private final CoachMapper coachMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CoachResponse createCoach(CoachRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Coach@123"))
                .role(Role.FITNESS_COACH)
                .active(request.getActive() == null || request.getActive())
                .build();
        user = userRepository.save(user);

        FitnessCoach coach = FitnessCoach.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .build();
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    @Transactional
    public CoachResponse updateCoach(Long id, CoachRequest request) {
        FitnessCoach coach = getCoachEntityById(id);
        User user = coach.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        coach.setSpecialization(request.getSpecialization());
        coach.setExperienceYears(request.getExperienceYears());
        coach.setBio(request.getBio());
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    @Transactional
    public void deleteCoach(Long id) {
        FitnessCoach coach = getCoachEntityById(id);
        coachRepository.delete(coach);
        userRepository.delete(coach.getUser());
    }

    @Override
    public CoachResponse getCoachById(Long id) {
        return coachMapper.toResponse(getCoachEntityById(id));
    }

    @Override
    public PageResponse<CoachResponse> getCoaches(String keyword, int page, int size) {
        Page<FitnessCoach> coaches = coachRepository.search(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(coaches.map(coachMapper::toResponse));
    }

    @Override
    public FitnessCoach getCoachEntityById(Long id) {
        return coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness Coach", "id", id));
    }

    @Override
    public FitnessCoach getCoachEntityByUserId(Long userId) {
        return coachRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness Coach", "userId", userId));
    }
}
