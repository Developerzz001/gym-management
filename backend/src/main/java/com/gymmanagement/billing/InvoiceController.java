package com.gymmanagement.billing;

import com.gymmanagement.billing.dto.AmountRemarksRequest;
import com.gymmanagement.billing.dto.CreateInvoiceRequest;
import com.gymmanagement.billing.dto.InvoiceResponse;
import com.gymmanagement.billing.dto.RecordPaymentRequest;
import com.gymmanagement.billing.dto.ShareInvoiceRequest;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.user.UserRepository;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices and Payments", description = "Invoice lifecycle, payment collection, refunds and waivers")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @Operation(summary = "Create an invoice")
    public ApiResponse<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return ApiResponse.success("Invoice created", invoiceService.create(request));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @Operation(summary = "Record a full or partial payment")
    public ApiResponse<InvoiceResponse> pay(@PathVariable Long id, @Valid @RequestBody RecordPaymentRequest request) {
        return ApiResponse.success("Payment recorded", invoiceService.recordPayment(id, request));
    }

    @PostMapping("/{id}/waivers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InvoiceResponse> waive(@PathVariable Long id, @Valid @RequestBody AmountRemarksRequest request) {
        return ApiResponse.success("Balance waived", invoiceService.waive(id, request));
    }

    @PostMapping("/{id}/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InvoiceResponse> refund(@PathVariable Long id, @Valid @RequestBody AmountRemarksRequest request) {
        return ApiResponse.success("Refund recorded", invoiceService.refund(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<PageResponse<InvoiceResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(invoiceService.list(page, size));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<PageResponse<InvoiceResponse>> mine(Authentication authentication,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName())).getId();
        Long clientId = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "userId", userId)).getId();
        return ApiResponse.success(invoiceService.listForClient(clientId, page, size));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.get(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoice.invoiceNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(invoiceService.pdf(id));
    }

            @GetMapping("/mine/{id}/pdf")
            @PreAuthorize("hasRole('CLIENT')")
            public ResponseEntity<byte[]> downloadMyPdf(@PathVariable Long id, Authentication authentication) {
            Long userId = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName())).getId();
            Long clientId = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "userId", userId)).getId();
            InvoiceResponse invoice = invoiceService.get(id);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoice.invoiceNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(invoiceService.pdfForClient(id, clientId));
            }

    @PostMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> email(@PathVariable Long id, @Valid @RequestBody ShareInvoiceRequest request) {
        invoiceService.shareByEmail(id, request.email());
        return ApiResponse.success("Invoice emailed", null);
    }
}