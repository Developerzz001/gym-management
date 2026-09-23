package com.gymmanagement.membership;

import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.membership.dto.MembershipPlanRequest;
import com.gymmanagement.membership.dto.MembershipPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipPlanMapper membershipPlanMapper;

    @Override
    @Transactional
    public MembershipPlanResponse createPlan(MembershipPlanRequest request) {
        MembershipPlan plan = MembershipPlan.builder()
                .name(request.getName())
                .durationDays(request.getDurationDays())
                .fees(request.getFees())
                .extraDurationDays(orZero(request.getExtraDurationDays()))
                .description(request.getDescription())
                .build();
        return membershipPlanMapper.toResponse(membershipPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public MembershipPlanResponse updatePlan(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = getPlanEntityById(id);
        plan.setName(request.getName());
        plan.setDurationDays(request.getDurationDays());
        plan.setFees(request.getFees());
        plan.setExtraDurationDays(orZero(request.getExtraDurationDays()));
        plan.setDescription(request.getDescription());
        return membershipPlanMapper.toResponse(membershipPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        membershipPlanRepository.delete(getPlanEntityById(id));
    }

    @Override
    public List<MembershipPlanResponse> getAllPlans() {
        return membershipPlanRepository.findAll(Sort.by("id").descending()).stream()
                .map(membershipPlanMapper::toResponse)
                .toList();
    }

    @Override
    public MembershipPlan getPlanEntityById(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership Plan", "id", id));
    }

    private Integer orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
