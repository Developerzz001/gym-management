package com.gymmanagement.membership;

import com.gymmanagement.membership.dto.MembershipDiscountRequest;
import com.gymmanagement.membership.dto.MembershipDiscountResponse;

import java.util.List;

public interface MembershipDiscountService {

    MembershipDiscountResponse create(MembershipDiscountRequest request);

    MembershipDiscountResponse update(Long id, MembershipDiscountRequest request);

    MembershipDiscountResponse setActive(Long id, boolean active);

    List<MembershipDiscountResponse> list(boolean activeOnly);

    MembershipDiscount getActiveEntity(Long id);
}