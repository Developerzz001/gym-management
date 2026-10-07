package com.gymmanagement.enrollment.dto;

import com.gymmanagement.billing.dto.InvoiceResponse;
import com.gymmanagement.client.dto.ClientResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private ClientResponse client;
    private InvoiceResponse invoice;
}
