package com.gymmanagement.membership;

import com.gymmanagement.membership.dto.ActivityRequest;
import com.gymmanagement.membership.dto.ActivityResponse;

import java.util.List;

public interface ActivityService {

    ActivityResponse create(ActivityRequest request);

    ActivityResponse update(Long id, ActivityRequest request);

    void delete(Long id);

    List<ActivityResponse> getAll();

    Activity getEntityById(Long id);
}
