package com.gymmanagement.client;

import com.gymmanagement.client.dto.FollowUpRequest;
import com.gymmanagement.client.dto.FollowUpResponse;

import java.util.List;

public interface FollowUpService {

    FollowUpResponse addFollowUp(Long clientId, FollowUpRequest request);

    List<FollowUpResponse> getFollowUps(Long clientId);
}
