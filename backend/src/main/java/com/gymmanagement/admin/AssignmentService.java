package com.gymmanagement.admin;

import com.gymmanagement.admin.dto.AssignCoachRequest;
import com.gymmanagement.admin.dto.AssignDieticianRequest;
import com.gymmanagement.client.dto.ClientResponse;

public interface AssignmentService {

    ClientResponse assignCoach(AssignCoachRequest request);

    ClientResponse assignDietician(AssignDieticianRequest request);
}
