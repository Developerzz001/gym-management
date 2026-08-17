package com.gymmanagement.membership;

import com.gymmanagement.membership.dto.MembershipPlanRequest;
import com.gymmanagement.membership.dto.MembershipPlanResponse;

import java.util.List;

public interface MembershipPlanService {

    MembershipPlanResponse createPlan(MembershipPlanRequest request);

    MembershipPlanResponse updatePlan(Long id, MembershipPlanRequest request);

    void deletePlan(Long id);

    List<MembershipPlanResponse> getAllPlans();

    MembershipPlan getPlanEntityById(Long id);
}
