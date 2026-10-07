package com.gymmanagement.enrollment;

import com.gymmanagement.enrollment.dto.ClientEnrollmentRequest;
import com.gymmanagement.enrollment.dto.EnrollmentResponse;

public interface EnrollmentService {

    /** Creates a new client and enrolls them into a membership plan with payment, in a single transaction. */
    EnrollmentResponse registerClientWithEnrollment(ClientEnrollmentRequest request);

    /** Reusable for future renewal/upgrade/transfer flows: enrolls an existing client into a membership plan. */
    EnrollmentResponse enrollExistingClient(Long clientId, ClientEnrollmentRequest request);
}
