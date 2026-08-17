package com.gymmanagement.progress;

import com.gymmanagement.progress.dto.ProgressRecordRequest;
import com.gymmanagement.progress.dto.ProgressRecordResponse;

import java.util.List;

public interface ProgressRecordService {

    ProgressRecordResponse addProgress(String coachEmail, ProgressRecordRequest request);

    ProgressRecordResponse updateProgress(String coachEmail, Long id, ProgressRecordRequest request);

    List<ProgressRecordResponse> getByClient(Long clientId);
}
