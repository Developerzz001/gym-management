package com.gymmanagement.membership;

import com.gymmanagement.billing.Invoice;
import com.gymmanagement.membership.dto.AssignMembershipRequest;
import com.gymmanagement.membership.dto.MembershipResponse;

import java.util.List;

public interface MembershipService {

    MembershipResponse assignMembership(AssignMembershipRequest request);

    MembershipResponse renewMembership(Long clientId);

    Membership activateFromInvoice(Invoice invoice);

    List<MembershipResponse> getByClient(Long clientId);
}
