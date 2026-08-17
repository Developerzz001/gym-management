package com.gymmanagement.membership;

import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.membership.dto.AssignMembershipRequest;
import com.gymmanagement.membership.dto.MembershipResponse;
import com.gymmanagement.notification.NotificationService;
import com.gymmanagement.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final ClientService clientService;
    private final MembershipPlanService membershipPlanService;
    private final MembershipMapper membershipMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public MembershipResponse assignMembership(AssignMembershipRequest request) {
        Client client = clientService.getClientEntityById(request.getClientId());
        MembershipPlan plan = membershipPlanService.getPlanEntityById(request.getMembershipPlanId());

        Membership membership = Membership.builder()
                .client(client)
                .membershipPlan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusDays(plan.getDurationDays()))
                .status(MembershipStatus.ACTIVE)
                .build();
        return membershipMapper.toResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public MembershipResponse renewMembership(Long clientId) {
        Membership latest = membershipRepository.findFirstByClientIdOrderByEndDateDesc(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "clientId", clientId));
        latest.setStatus(MembershipStatus.EXPIRED);
        membershipRepository.save(latest);

        Membership renewed = Membership.builder()
                .client(latest.getClient())
                .membershipPlan(latest.getMembershipPlan())
                .startDate(latest.getEndDate())
                .endDate(latest.getEndDate().plusDays(latest.getMembershipPlan().getDurationDays()))
                .status(MembershipStatus.ACTIVE)
                .build();
        Membership saved = membershipRepository.save(renewed);
        notificationService.createNotification(latest.getClient().getUser(), NotificationType.MEMBERSHIP_EXPIRY,
                "Your membership has been renewed until " + saved.getEndDate());
        return membershipMapper.toResponse(saved);
    }

    @Override
    public List<MembershipResponse> getByClient(Long clientId) {
        return membershipRepository.findByClientIdOrderByStartDateDesc(clientId).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }
}
