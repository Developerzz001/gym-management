package com.gymmanagement.dietician;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dietician.dto.DieticianRequest;
import com.gymmanagement.dietician.dto.DieticianResponse;
import com.gymmanagement.dietician.dto.DieticianProfileRequest;
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
public class DieticianServiceImpl implements DieticianService {

    private final DieticianRepository dieticianRepository;
    private final UserRepository userRepository;
    private final DieticianMapper dieticianMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public DieticianResponse createDietician(DieticianRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Diet@123"))
                .role(Role.DIETICIAN)
                .active(request.getActive() == null || request.getActive())
                .build();
        user = userRepository.save(user);

        Dietician dietician = Dietician.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .build();
        return dieticianMapper.toResponse(dieticianRepository.save(dietician));
    }

    @Override
    @Transactional
    public DieticianResponse updateDietician(Long id, DieticianRequest request) {
        Dietician dietician = getDieticianEntityById(id);
        User user = dietician.getUser();
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

        dietician.setSpecialization(request.getSpecialization());
        dietician.setExperienceYears(request.getExperienceYears());
        dietician.setBio(request.getBio());
        return dieticianMapper.toResponse(dieticianRepository.save(dietician));
    }

    @Override
    @Transactional
    public DieticianResponse activateDietician(Long id) {
        Dietician dietician = getDieticianEntityById(id);
        dietician.getUser().setActive(true);
        userRepository.save(dietician.getUser());
        return dieticianMapper.toResponse(dietician);
    }

    @Override
    @Transactional
    public DieticianResponse deactivateDietician(Long id) {
        Dietician dietician = getDieticianEntityById(id);
        dietician.getUser().setActive(false);
        userRepository.save(dietician.getUser());
        return dieticianMapper.toResponse(dietician);
    }

    @Override
    @Transactional
    public void deleteDietician(Long id) {
        Dietician dietician = getDieticianEntityById(id);
        dieticianRepository.delete(dietician);
        userRepository.delete(dietician.getUser());
    }

    @Override
    public DieticianResponse getDieticianById(Long id) {
        return dieticianMapper.toResponse(getDieticianEntityById(id));
    }

    @Override
    public DieticianResponse getDieticianByUserId(Long userId) {
        return dieticianMapper.toResponse(getDieticianEntityByUserId(userId));
    }

    @Override
    @Transactional
    public DieticianResponse updateOwnProfile(Long userId, DieticianProfileRequest request) {
        Dietician dietician = getDieticianEntityByUserId(userId);
        User user = dietician.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        dietician.setSpecialization(request.getSpecialization());
        dietician.setExperienceYears(request.getExperienceYears());
        dietician.setBio(request.getBio());
        return dieticianMapper.toResponse(dieticianRepository.save(dietician));
    }

    @Override
    public PageResponse<DieticianResponse> getDieticians(String keyword, int page, int size) {
        Page<Dietician> dieticians = dieticianRepository.search(keyword == null ? "" : keyword,
                PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(dieticians.map(dieticianMapper::toResponse));
    }

    @Override
    public Dietician getDieticianEntityById(Long id) {
        return dieticianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", id));
    }

    @Override
    public Dietician getDieticianEntityByUserId(Long userId) {
        return dieticianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "userId", userId));
    }
}
