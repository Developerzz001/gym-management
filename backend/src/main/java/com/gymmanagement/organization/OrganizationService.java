package com.gymmanagement.organization;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.organization.dto.OrganizationRequest;
import com.gymmanagement.organization.dto.OrganizationResponse;
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
public class OrganizationService {

    private final OrganizationRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A user with email '" + request.email() + "' already exists");
        }
        if (request.adminPassword() == null || request.adminPassword().isBlank()) {
            throw new BadRequestException("Organization admin password is required");
        }
        String code = "ORG_%02d".formatted(repository.findNextGeneratedCodeNumber());
        Organization organization = Organization.builder().code(code).name(request.name())
                .ownerName(request.ownerName()).contactNumber(request.contactNumber()).email(request.email())
                .address(request.address()).build();
        organization = repository.save(organization);
        String[] ownerNames = request.ownerName().trim().split("\\s+", 2);
        userRepository.save(User.builder()
                .firstName(ownerNames[0])
                .lastName(ownerNames.length > 1 ? ownerNames[1] : "Administrator")
                .email(request.email())
                .mobileNumber(request.contactNumber())
                .password(passwordEncoder.encode(request.adminPassword()))
                .role(Role.ORGANIZATION_ADMIN)
                .organization(organization)
                .active(true)
                .build());
        return response(organization);
    }

    @Transactional
    public OrganizationResponse update(Long id, OrganizationRequest request) {
        Organization organization = getEntity(id);
        organization.setName(request.name());
        organization.setOwnerName(request.ownerName());
        organization.setContactNumber(request.contactNumber());
        organization.setEmail(request.email());
        organization.setAddress(request.address());
        return response(repository.save(organization));
    }

    @Transactional
    public OrganizationResponse setActive(Long id, boolean active) {
        Organization organization = getEntity(id);
        organization.setStatus(active ? OrganizationStatus.ACTIVE : OrganizationStatus.INACTIVE);
        return response(repository.save(organization));
    }

    public OrganizationResponse get(Long id) { return response(getEntity(id)); }

    public PageResponse<OrganizationResponse> list(String keyword, int page, int size) {
        String search = keyword == null ? "" : keyword.trim();
        Page<Organization> result = repository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                search, search, PageRequest.of(page, Math.min(size, 100), Sort.by("name")));
        return PageResponse.from(result.map(this::response));
    }

    public Organization getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
    }

    private OrganizationResponse response(Organization value) {
        return new OrganizationResponse(value.getId(), value.getCode(), value.getName(), value.getOwnerName(),
                value.getContactNumber(), value.getEmail(), value.getAddress(), value.getStatus(),
                value.getCreatedAt(), value.getUpdatedAt());
    }
}