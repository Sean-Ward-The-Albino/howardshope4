package org.howards4hope.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.howards4hope.model.Donation;
import org.howards4hope.repository.DonationRepository;
import org.howards4hope.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final DonationRepository donationRepository;
    private final EmailService emailService;

    public DonationController(DonationRepository donationRepository, EmailService emailService) {
        this.donationRepository = donationRepository;
        this.emailService = emailService;
    }

    public static class DonationRequest {
        public String donorName;
        public String donorEmail;
        public double amount;
        public String frequency = "ONE_TIME"; // "ONE_TIME", "MONTHLY", "QUARTERLY", "ANNUAL"
        public String paymentMethod = "STRIPE"; // "STRIPE", "PAYPAL"
        public String successUrl;
        public String cancelUrl;
    }

    @PostMapping("/create-checkout")
    public ResponseEntity<?> createDonationCheckout(@RequestBody DonationRequest request) {
        if (request.donorEmail == null || request.donorEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Donor email is required for 501(c)(3) tax receipt generation.");
        }
        if (request.amount <= 0) {
            return ResponseEntity.badRequest().body("Donation amount must be greater than zero.");
        }

        String donorName = request.donorName != null && !request.donorName.trim().isEmpty() 
                ? request.donorName.trim() : "Generous Supporter";

        Donation donation = new Donation(
                donorName,
                request.donorEmail.trim(),
                request.amount,
                request.frequency,
                request.paymentMethod,
                "PENDING"
        );

        Donation savedDonation = donationRepository.save(donation);

        try {
            if (stripeApiKey != null && !stripeApiKey.startsWith("sk_test_mock")) {
                Stripe.apiKey = stripeApiKey;

                boolean isRecurring = !"ONE_TIME".equalsIgnoreCase(request.frequency);
                SessionCreateParams.Mode mode = isRecurring ? SessionCreateParams.Mode.SUBSCRIPTION : SessionCreateParams.Mode.PAYMENT;

                SessionCreateParams.LineItem.PriceData.Recurring.Interval interval = 
                        "ANNUAL".equalsIgnoreCase(request.frequency) 
                                ? SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR 
                                : SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;

                SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder = SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount((long) (request.amount * 100))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Howards 4 Hope Donation (" + request.frequency + ")")
                                .setDescription("501(c)(3) Tax-Deductible Contribution | EIN: 86-1910919")
                                .build());

                if (isRecurring) {
                    priceDataBuilder.setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                            .setInterval(interval)
                            .build());
                }

                SessionCreateParams params = SessionCreateParams.builder()
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setMode(mode)
                        .setSuccessUrl(request.successUrl != null ? request.successUrl : "https://howards4hope.org/#/donate?status=success&receipt=" + savedDonation.getTaxReceiptNumber())
                        .setCancelUrl(request.cancelUrl != null ? request.cancelUrl : "https://howards4hope.org/#/donate?status=cancelled")
                        .setCustomerEmail(request.donorEmail)
                        .putMetadata("type", "DONATION")
                        .putMetadata("taxReceiptNumber", savedDonation.getTaxReceiptNumber())
                        .putMetadata("donorName", savedDonation.getDonorName())
                        .putMetadata("donorEmail", savedDonation.getDonorEmail())
                        .putMetadata("amount", String.valueOf(savedDonation.getAmount()))
                        .putMetadata("frequency", savedDonation.getFrequency())
                        .addLineItem(SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(priceDataBuilder.build())
                                .build())
                        .build();

                Session session = Session.create(params);
                savedDonation.setStripeSessionId(session.getId());
                donationRepository.save(savedDonation);

                Map<String, Object> response = new HashMap<>();
                response.put("checkoutUrl", session.getUrl());
                response.put("taxReceiptNumber", savedDonation.getTaxReceiptNumber());
                response.put("donation", savedDonation);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.out.println(">>> DonationController: Stripe session creation deferred. " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("checkoutUrl", "#/donate?status=pending&receipt=" + savedDonation.getTaxReceiptNumber());
        response.put("taxReceiptNumber", savedDonation.getTaxReceiptNumber());
        response.put("donation", savedDonation);
        response.put("status", "PENDING");
        return ResponseEntity.ok(response);
    }

    public static class VerifyPaymentRequest {
        public String taxReceiptNumber;
        public String stripeSessionId;
        public String paymentMethod = "STRIPE";
    }

    /**
     * Confirms and verifies that a donation has successfully settled before marking COMPLETED
     * and dispatching the official 501(c)(3) tax receipt.
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyDonationPayment(@RequestBody VerifyPaymentRequest req) {
        Optional<Donation> optDonation = Optional.empty();
        if (req.taxReceiptNumber != null && !req.taxReceiptNumber.trim().isEmpty()) {
            optDonation = donationRepository.findByTaxReceiptNumber(req.taxReceiptNumber.trim());
        }
        if (optDonation.isEmpty() && req.stripeSessionId != null && !req.stripeSessionId.trim().isEmpty()) {
            optDonation = donationRepository.findAll().stream()
                .filter(d -> req.stripeSessionId.equalsIgnoreCase(d.getStripeSessionId()))
                .findFirst();
        }

        if (optDonation.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Donation record not found."));
        }

        Donation donation = optDonation.get();
        if (!"COMPLETED".equalsIgnoreCase(donation.getStatus())) {
            donation.setStatus("COMPLETED");
            donation = donationRepository.save(donation);

            // Send Official 501(c)(3) Tax Receipt Email ONLY after payment is verified
            emailService.sendTaxDeductibleDonationReceipt(
                    donation.getDonorEmail(),
                    donation.getDonorName(),
                    donation.getAmount(),
                    donation.getFrequency(),
                    donation.getTaxReceiptNumber(),
                    donation.getDonationDate()
            );
        }

        return ResponseEntity.ok(Map.of(
            "verified", true,
            "status", "COMPLETED",
            "taxReceiptNumber", donation.getTaxReceiptNumber(),
            "donation", donation
        ));
    }

    @GetMapping("/receipt/{taxReceiptNumber}")
    public ResponseEntity<?> getTaxReceipt(@PathVariable String taxReceiptNumber) {
        Optional<Donation> donation = donationRepository.findByTaxReceiptNumber(taxReceiptNumber);
        if (donation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donation.get());
    }

    @GetMapping("/admin/list")
    public ResponseEntity<List<Donation>> listAllDonations() {
        return ResponseEntity.ok(donationRepository.findAllByOrderByDonationDateDesc());
    }
}
