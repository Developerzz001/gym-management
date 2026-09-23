package com.gymmanagement.membership;

import com.gymmanagement.billing.Invoice;
import com.gymmanagement.billing.InvoiceType;
import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.client.RegistrationType;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.membership.dto.AssignMembershipRequest;
import com.gymmanagement.membership.dto.MembershipResponse;
import com.gymmanagement.notification.AutomatedNotificationService;
import com.gymmanagement.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final ClientService clientService;
    private final MembershipPlanService membershipPlanService;
    private final MembershipMapper membershipMapper;
        private final AutomatedNotificationService notificationService;

    @Override
    @Transactional
    public MembershipResponse assignMembership(AssignMembershipRequest request) {
        Client client = clientService.getClientEntityById(request.getClientId());
                if (client.getRegistrationType() == RegistrationType.INQUIRY) {
                        throw new IllegalArgumentException("Membership can only be assigned to a registered client");
                }
        MembershipPlan plan = membershipPlanService.getPlanEntityById(request.getMembershipPlanId());

        Membership membership = Membership.builder()
                .client(client)
                .branch(client.getUser().getBranch())
                .membershipPlan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusDays(plan.getDurationDays()))
                .status(MembershipStatus.ACTIVE)
                .build();
        Membership saved = membershipRepository.save(membership);
        client.getUser().setActive(true);
        return membershipMapper.toResponse(saved);
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
                .branch(latest.getClient().getUser().getBranch())
                .membershipPlan(latest.getMembershipPlan())
                .startDate(latest.getEndDate())
                .endDate(latest.getEndDate().plusDays(latest.getMembershipPlan().getDurationDays()))
                .status(MembershipStatus.ACTIVE)
                .build();
        Membership saved = membershipRepository.save(renewed);
        notificationService.send(latest.getClient().getUser(), NotificationType.MEMBERSHIP_RENEWED,
                "Membership renewed", "Your membership has been renewed until " + saved.getEndDate(),
                "MEMBERSHIP-" + saved.getId() + "-RENEWED");
        return membershipMapper.toResponse(saved);
    }

        @Override
        @Transactional
        public Membership activateFromInvoice(Invoice invoice) {
                if (invoice.getMembershipPlan() == null || invoice.getServiceStartDate() == null) {
                        throw new IllegalArgumentException("Membership invoice requires a plan and start date");
                }
                LocalDate startDate = invoice.getServiceStartDate();
                if (invoice.getInvoiceType() == InvoiceType.MEMBERSHIP_RENEWAL) {
                        Membership latest = membershipRepository.findFirstByClientIdOrderByEndDateDesc(invoice.getClient().getId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Membership", "clientId", invoice.getClient().getId()));
                        startDate = latest.getEndDate().plusDays(1);
                }
                int extraDurationDays = invoice.getMembershipPlan().getExtraDurationDays() == null
                        ? 0 : invoice.getMembershipPlan().getExtraDurationDays();
                int discountFreeDays = invoice.getMembershipDiscount() == null
                        || invoice.getMembershipDiscount().getExtraFreeDays() == null
                        ? 0 : invoice.getMembershipDiscount().getExtraFreeDays();
                int durationDays = invoice.getMembershipPlan().getDurationDays() + extraDurationDays + discountFreeDays;
                Membership membership = Membership.builder()
                                .client(invoice.getClient())
                                .branch(invoice.getClient().getUser().getBranch())
                                .membershipPlan(invoice.getMembershipPlan())
                                .startDate(startDate)
                                .endDate(startDate.plusDays(durationDays - 1L))
                                .status(MembershipStatus.ACTIVE)
                                .build();
                Membership saved = membershipRepository.save(membership);
                invoice.getClient().getUser().setActive(true);
                notificationService.send(invoice.getClient().getUser(), NotificationType.MEMBERSHIP_RENEWED,
                                invoice.getInvoiceType() == InvoiceType.MEMBERSHIP_RENEWAL ? "Membership renewed" : "Membership activated",
                                "Your membership is active until " + saved.getEndDate(),
                                "INVOICE-" + invoice.getId() + "-MEMBERSHIP-ACTIVATED");
                return saved;
        }

    @Override
    public List<MembershipResponse> getByClient(Long clientId) {
        return membershipRepository.findByClientIdOrderByStartDateDesc(clientId).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }
}
