package com.gymmanagement.enrollment;

import com.gymmanagement.billing.InvoiceService;
import com.gymmanagement.billing.InvoiceType;
import com.gymmanagement.billing.dto.CreateInvoiceRequest;
import com.gymmanagement.billing.dto.InvoiceResponse;
import com.gymmanagement.client.ClientService;
import com.gymmanagement.client.dto.ClientRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.enrollment.dto.ClientEnrollmentRequest;
import com.gymmanagement.enrollment.dto.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final ClientService clientService;
    private final InvoiceService invoiceService;

    @Override
    public EnrollmentResponse registerClientWithEnrollment(ClientEnrollmentRequest request) {
        ClientRequest clientRequest = ClientRequest.builder()
                .branchId(request.getBranchId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .active(true)
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .alternateContactNumber(request.getAlternateContactNumber())
                .fitnessGoal(request.getFitnessGoal())
                .executiveId(request.getExecutiveId())
                .allergies(request.getAllergies())
                .injuries(request.getInjuries())
                .medicalNotes(request.getMedicalNotes())
                .build();
        ClientResponse client = clientService.registerClient(clientRequest);
        InvoiceResponse invoice = createEnrollmentInvoice(client.getId(),
                client.getFirstName() + " " + client.getLastName(), request);
        return EnrollmentResponse.builder().client(client).invoice(invoice).build();
    }

    @Override
    public EnrollmentResponse enrollExistingClient(Long clientId, ClientEnrollmentRequest request) {
        ClientResponse client = clientService.getClientById(clientId);
        InvoiceResponse invoice = createEnrollmentInvoice(clientId,
                client.getFirstName() + " " + client.getLastName(), request);
        return EnrollmentResponse.builder().client(client).invoice(invoice).build();
    }

    private InvoiceResponse createEnrollmentInvoice(Long clientId, String clientName, ClientEnrollmentRequest request) {
        String description = request.getDescription() != null && !request.getDescription().isBlank()
                ? request.getDescription()
                : "Membership enrollment for " + clientName;
        LocalDate dueDate = request.getStartDate().isBefore(LocalDate.now()) ? LocalDate.now() : request.getStartDate();
        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest(
                clientId,
                InvoiceType.MEMBERSHIP,
                description,
                null,
                request.getTax(),
                BigDecimal.ZERO,
                dueDate,
                request.getMembershipPlanId(),
                request.getMembershipDiscountId(),
                request.getStartDate(),
                null,
                null,
                request.getAmountPaid(),
                request.getPaymentMethod(),
                request.getTransactionReference(),
                request.getRemarks());
        return invoiceService.create(invoiceRequest);
    }
}
