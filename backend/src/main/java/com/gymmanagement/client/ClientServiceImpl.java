package com.gymmanagement.client;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.branch.BranchRepository;
import com.gymmanagement.client.dto.ClientRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.client.dto.ClientProfileRequest;
import com.gymmanagement.coach.CoachService;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dietician.DieticianService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import com.gymmanagement.security.TenantAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final CoachService coachService;
    private final DieticianService dieticianService;
    private final BranchRepository branchRepository;
    private final TenantAccessService tenantAccess;

    @Override
    @Transactional
    public ClientResponse registerClient(ClientRequest request) {
        request.setRegistrationType(RegistrationType.REGISTERED);
        return createClient(request);
    }

    @Override
    @Transactional
    public ClientResponse registerInquiry(ClientRequest request) {
        request.setRegistrationType(RegistrationType.INQUIRY);
        request.setActive(false);
        return createClient(request);
    }

    private ClientResponse createClient(ClientRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        Branch branch = resolveRegistrationBranch(request.getBranchId());
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getContactNumber())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Client@123"))
                .role(Role.CLIENT)
                .organization(branch == null ? null : branch.getOrganization())
                .branch(branch)
                .active(request.getActive() == null || request.getActive())
                .build();
        user = userRepository.save(user);

        Client client = Client.builder()
                .user(user)
                .registrationType(request.getRegistrationType() == null
                        ? RegistrationType.REGISTERED : request.getRegistrationType())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .fitnessGoal(request.getFitnessGoal())
                .diabetes(request.isDiabetes())
                .hypertension(request.isHypertension())
                .asthma(request.isAsthma())
                .allergies(request.getAllergies())
                .injuries(request.getInjuries())
                .medicalNotes(request.getMedicalNotes())
                .build();
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = getClientEntityById(id);
        User user = client.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        client.setGender(request.getGender());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setHeightCm(request.getHeightCm());
        client.setWeightKg(request.getWeightKg());
        client.setAddress(request.getAddress());
        client.setContactNumber(request.getContactNumber());
        client.setFitnessGoal(request.getFitnessGoal());
        client.setDiabetes(request.isDiabetes());
        client.setHypertension(request.isHypertension());
        client.setAsthma(request.isAsthma());
        client.setAllergies(request.getAllergies());
        client.setInjuries(request.getInjuries());
        client.setMedicalNotes(request.getMedicalNotes());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse deactivateClient(Long id) {
        Client client = getClientEntityById(id);
        client.getUser().setActive(false);
        userRepository.save(client.getUser());
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse activateClient(Long id) {
        Client client = getClientEntityById(id);
        client.getUser().setActive(true);
        userRepository.save(client.getUser());
        return clientMapper.toResponse(client);
    }

    @Override
    public ClientResponse getClientById(Long id) {
        Client client = getClientEntityById(id);
        if (client.getUser().getBranch() != null) {
            tenantAccess.assertBranchAccess(client.getUser().getBranch());
        }
        return clientMapper.toResponse(client);
    }

    @Override
    public ClientResponse getClientByUserId(Long userId) {
        return clientMapper.toResponse(getClientEntityByUserId(userId));
    }

    @Override
    @Transactional
    public ClientResponse updateOwnProfile(Long userId, ClientProfileRequest request) {
        Client client = getClientEntityByUserId(userId);
        User user = client.getUser();
        updateUserDetails(user, request.getFirstName(), request.getLastName(), request.getEmail(),
                request.getContactNumber(), request.getPassword());
        client.setGender(request.getGender());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setHeightCm(request.getHeightCm());
        client.setWeightKg(request.getWeightKg());
        client.setAddress(request.getAddress());
        client.setContactNumber(request.getContactNumber());
        client.setFitnessGoal(request.getFitnessGoal());
        client.setDiabetes(request.isDiabetes());
        client.setHypertension(request.isHypertension());
        client.setAsthma(request.isAsthma());
        client.setAllergies(request.getAllergies());
        client.setInjuries(request.getInjuries());
        client.setMedicalNotes(request.getMedicalNotes());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void updateProfileImage(Long id, byte[] image, String contentType) {
        Client client = getClientEntityById(id);
        if (client.getUser().getBranch() != null) {
            tenantAccess.assertBranchAccess(client.getUser().getBranch());
        }
        client.getUser().setProfileImage(image);
        client.getUser().setProfileImageContentType(contentType);
        userRepository.save(client.getUser());
    }

    private void updateUserDetails(User user, String firstName, String lastName, String email,
                                   String mobileNumber, String password) {
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user with email '" + email + "' already exists");
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
    }

    @Override
    public PageResponse<ClientResponse> getClients(String keyword, RegistrationType registrationType, int page, int size) {
        String search = keyword == null ? "" : keyword;
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("id").descending());
        User currentUser = tenantAccess.currentUser();
        Page<Client> clients;
        if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.ADMIN) {
            clients = clientRepository.search(search, registrationType, pageable);
        } else if (currentUser.getRole() == Role.ORGANIZATION_ADMIN) {
            clients = clientRepository.searchByOrganization(currentUser.getOrganization().getId(), search, registrationType, pageable);
        } else {
            if (currentUser.getBranch() == null) {
                throw new org.springframework.security.access.AccessDeniedException("Branch assignment is required");
            }
            clients = clientRepository.searchByBranch(currentUser.getBranch().getId(), search, registrationType, pageable);
        }
        return PageResponse.from(clients.map(clientMapper::toResponse));
    }

    private Branch resolveRegistrationBranch(Long requestedBranchId) {
        User currentUser = tenantAccess.currentUser();
        if (requestedBranchId == null) {
            if (currentUser.getRole() == Role.ADMIN) return null;
            if (currentUser.getBranch() == null) {
                throw new com.gymmanagement.common.exception.BadRequestException("branchId is required");
            }
            return currentUser.getBranch();
        }
        Branch branch = branchRepository.findById(requestedBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", requestedBranchId));
        tenantAccess.assertBranchAccess(branch);
        return branch;
    }

    @Override
    @Transactional
    public ClientResponse convertInquiry(Long id, ClientRequest request) {
        Client client = getClientEntityById(id);
        if (client.getRegistrationType() != RegistrationType.INQUIRY) {
            throw new IllegalArgumentException("Client is already registered");
        }
        User user = client.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getContactNumber());
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        client.setRegistrationType(RegistrationType.REGISTERED);
        client.setGender(request.getGender());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setHeightCm(request.getHeightCm());
        client.setWeightKg(request.getWeightKg());
        client.setAddress(request.getAddress());
        client.setContactNumber(request.getContactNumber());
        client.setFitnessGoal(request.getFitnessGoal());
        client.setDiabetes(request.isDiabetes());
        client.setHypertension(request.isHypertension());
        client.setAsthma(request.isAsthma());
        client.setAllergies(request.getAllergies());
        client.setInjuries(request.getInjuries());
        client.setMedicalNotes(request.getMedicalNotes());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    public List<ClientResponse> getClientsByCoach(String coachEmail) {
        Long coachUserId = userRepository.findByEmailIgnoreCase(coachEmail).orElseThrow().getId();
        Long coachId = coachService.getCoachEntityByUserId(coachUserId).getId();
        return clientRepository.findRegisteredByAssignedCoachId(coachId).stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Override
    public List<ClientResponse> getClientsByDietician(String dieticianEmail) {
        Long dieticianUserId = userRepository.findByEmailIgnoreCase(dieticianEmail).orElseThrow().getId();
        Long dieticianId = dieticianService.getDieticianEntityByUserId(dieticianUserId).getId();
        return clientRepository.findRegisteredByAssignedDieticianId(dieticianId).stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Override
    public Client getClientEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }

    @Override
    public Client getClientEntityByUserId(Long userId) {
        return clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "userId", userId));
    }
}
