package com.gymmanagement.client;

import com.gymmanagement.client.dto.FollowUpRequest;
import com.gymmanagement.client.dto.FollowUpResponse;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final FollowUpMapper followUpMapper;
    private final TenantAccessService tenantAccess;

    @Override
    @Transactional
    public FollowUpResponse addFollowUp(Long clientId, FollowUpRequest request) {
        Client client = getClientWithAccessCheck(clientId);
        User executive = request.getExecutiveId() != null
                ? userRepository.findById(request.getExecutiveId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getExecutiveId()))
                : tenantAccess.currentUser();

        FollowUp followUp = FollowUp.builder()
                .client(client)
                .followUpDate(request.getFollowUpDate() != null ? request.getFollowUpDate() : LocalDate.now())
                .comment(request.getComment())
                .executive(executive)
                .nextFollowUpDate(request.getNextFollowUpDate())
                .rating(request.getRating())
                .build();
        followUp = followUpRepository.save(followUp);

        client.setRating(request.getRating());
        client.setNextFollowUpDate(request.getNextFollowUpDate());
        client.setAssignedExecutive(executive);
        clientRepository.save(client);

        return followUpMapper.toResponse(followUp);
    }

    @Override
    public List<FollowUpResponse> getFollowUps(Long clientId) {
        getClientWithAccessCheck(clientId);
        return followUpRepository.findByClientIdOrderByFollowUpDateDescCreatedAtDesc(clientId).stream()
                .map(followUpMapper::toResponse)
                .toList();
    }

    private Client getClientWithAccessCheck(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
        if (client.getUser().getBranch() != null) {
            tenantAccess.assertBranchAccess(client.getUser().getBranch());
        }
        return client;
    }
}
