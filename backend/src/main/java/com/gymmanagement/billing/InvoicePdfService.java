package com.gymmanagement.billing;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

    public byte[] generate(Invoice invoice) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("GYM MANAGEMENT INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("Invoice: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Client: " + invoice.getClient().getUser().getFullName()));
            document.add(new Paragraph("Type: " + invoice.getInvoiceType().name().replace('_', ' ')));
            if (invoice.getMembershipPlan() != null) {
                document.add(new Paragraph("Membership plan: " + invoice.getMembershipPlan().getName()));
            }
            if (invoice.getMembershipDiscount() != null) {
                document.add(new Paragraph("Membership discount: " + invoice.getMembershipDiscount().getName()
                        + " (" + invoice.getMembershipDiscount().getPercentage() + "%)"));
            }
            if (invoice.getGeneratedMembership() != null) {
                document.add(new Paragraph("Membership ID: " + invoice.getGeneratedMembership().getId()));
            }
            if (invoice.getServiceStartDate() != null) {
                document.add(new Paragraph("Start date: " + invoice.getServiceStartDate()));
            }
            if (invoice.getSessionCount() != null) {
                document.add(new Paragraph("Sessions: " + invoice.getSessionCount()));
            }
            if (invoice.getPurchaseDate() != null) {
                document.add(new Paragraph("Purchase date: " + invoice.getPurchaseDate()));
            }
            document.add(new Paragraph("Due date: " + invoice.getDueDate()));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(invoice.getDescription()));
            document.add(new Paragraph("Subtotal: INR " + invoice.getTotalAmount()));
            document.add(new Paragraph("Tax: INR " + invoice.getTax()));
            document.add(new Paragraph("Discount: INR " + invoice.getDiscount()));
            document.add(new Paragraph("Final amount: INR " + invoice.getFinalAmount(), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.add(new Paragraph("Paid: INR " + invoice.getAmountPaid()));
            document.add(new Paragraph("Outstanding: INR " + invoice.getBalanceAmount()));
            document.add(new Paragraph("Status: " + invoice.getStatus()));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("PAYMENT HISTORY", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            for (PaymentTransaction transaction : invoice.getTransactions()) {
                document.add(new Paragraph(transaction.getPaymentDate() + " | " + transaction.getTransactionType()
                        + " | INR " + transaction.getPaidAmount() + " | " + transaction.getPaymentMethod()));
            }
            document.close();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate invoice PDF", exception);
        }
    }
}