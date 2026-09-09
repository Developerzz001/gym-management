package com.gymmanagement.client;

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
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getContactNumber())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Client@123"))
                .role(Role.CLIENT)
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
        return clientMapper.toResponse(getClientEntityById(id));
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
    public PageResponse<ClientResponse> getClients(String keyword, int page, int size) {
        Page<Client> clients = clientRepository.search(keyword == null ? "" : keyword,
            PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(clients.map(clientMapper::toResponse));
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
