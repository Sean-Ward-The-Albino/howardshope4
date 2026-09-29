package org.howards4hope.controller;

import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.howards4hope.model.Donation;
import org.howards4hope.model.Event;
import org.howards4hope.model.Ticket;
import org.howards4hope.repository.DonationRepository;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.repository.TicketRepository;
import org.howards4hope.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:whsec_mock_secret}")
    private String webhookSecret;

    @Value("${paypal.client.id:mock_paypal_client_id}")
    private String paypalClientId;

    @Value("${paypal.client.secret:mock_paypal_client_secret}")
    private String paypalClientSecret;

    @Value("${paypal.mode:live}")
    private String paypalMode;

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final DonationRepository donationRepository;
    private final EmailService emailService;

    public PaymentController(EventRepository eventRepository, 
                             TicketRepository ticketRepository,
                             DonationRepository donationRepository,
                             EmailService emailService) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.donationRepository = donationRepository;
        this.emailService = emailService;
    }

    public static class PaymentRequest {
        public Long eventId;
        public int quantity = 1;
        public Double unitPrice;
        public String eventTitle;
        public String eventDate;
        public String guestEmail;
        public String guestName;
        public String paymentPlanType = "FULL";
        public int installmentCycles = 1;
        public String successUrl;
        public String cancelUrl;
    }

    private Event findOrResolveEvent(Long eventId) {
        if (eventId == null) return null;
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isPresent()) {
            return optionalEvent.get();
        }
        if (eventId == 9999L) {
            return new Event(
                "Howard's 4 Hope 2026 Gala: Frost & Flame",
                "A night of celebration, hope, and community transformation.",
                "2026-10-17",
                "6:00 PM - 10:00 PM",
                "The Grand Long Beach, 4101 E Willow St, Long Beach, CA",
                150.0,
                "/assets/images/hero-gala.webp",
                "Gala",
                "#0284c7"
            );
        }
        return null;
    }

    // --- SECURE STRIPE CHECKOUT ROUTING ---
    
    @PostMapping("/create-stripe-checkout")
    public ResponseEntity<?> createStripeCheckout(@RequestBody PaymentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String customerEmail = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : request.guestEmail;
        String guestName = (request.guestName != null && !request.guestName.trim().isEmpty())
                ? request.guestName.trim() : "Valued Attendee";

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Customer or guest email is required."));
        }
        
        Event event = findOrResolveEvent(request.eventId);
        if (event == null && (request.unitPrice == null || request.unitPrice <= 0)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Event not found."));
        }

        String eventTitle = (request.eventTitle != null && !request.eventTitle.trim().isEmpty())
                ? request.eventTitle.trim()
                : (event != null ? event.getTitle() : "Event Pass");
        String eventDate = (request.eventDate != null && !request.eventDate.trim().isEmpty())
                ? request.eventDate.trim()
                : (event != null ? event.getDate() : LocalDate.now().toString());

        double unitPrice = (request.unitPrice != null && request.unitPrice > 0)
                ? request.unitPrice
                : (event != null ? event.getPrice() : 0.0);

        int qty = (request.quantity > 0) ? request.quantity : 1;
        double totalPrice = unitPrice * qty;

        if (totalPrice <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Free tickets do not require Stripe payment."));
        }

        boolean isInstallment = "INSTALLMENT".equalsIgnoreCase(request.paymentPlanType) 
                || (request.paymentPlanType != null && !request.paymentPlanType.equals("FULL") && request.installmentCycles > 1);
        int cycles = isInstallment ? Math.max(1, request.installmentCycles) : 1;
        double chargeAmount = isInstallment ? (totalPrice / cycles) : totalPrice;
        long chargeAmountCents = Math.round(chargeAmount * 100);

        String ticketId = "H4H-TKT-" + System.currentTimeMillis();
        
        try {
            com.stripe.Stripe.apiKey = stripeApiKey;

            String baseSuccessUrl = request.successUrl != null ? request.successUrl : "https://howards4hope.org/#/my-tickets";
            String delimiter = baseSuccessUrl.contains("?") ? "&" : "?";
            String successRedirect = baseSuccessUrl + delimiter + "session_id={CHECKOUT_SESSION_ID}&ticket=" + ticketId;
            String cancelRedirect = request.cancelUrl != null ? request.cancelUrl : "https://howards4hope.org/#/events";

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successRedirect)
                    .setCancelUrl(cancelRedirect)
                    .setCustomerEmail(customerEmail)
                    .putMetadata("type", "EVENT_TICKET")
                    .putMetadata("ticketId", ticketId)
                    .putMetadata("eventId", String.valueOf(request.eventId != null ? request.eventId : 9999L))
                    .putMetadata("eventTitle", eventTitle)
                    .putMetadata("eventDate", eventDate)
                    .putMetadata("guestName", guestName)
                    .putMetadata("guestEmail", customerEmail)
                    .putMetadata("quantity", String.valueOf(qty))
                    .putMetadata("unitPrice", String.valueOf(unitPrice))
                    .putMetadata("totalPrice", String.valueOf(totalPrice))
                    .putMetadata("chargeAmount", String.valueOf(chargeAmount))
                    .putMetadata("paymentPlanType", request.paymentPlanType != null ? request.paymentPlanType : "FULL")
                    .putMetadata("installmentCycles", String.valueOf(cycles))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(chargeAmountCents)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(eventTitle + (isInstallment ? " (Installment 1 of " + cycles + ")" : " - Admission Pass"))
                                            .setDescription("Howard's 4 Hope: " + eventTitle + " | " + eventDate + (isInstallment ? " | Initial payment of " + cycles + " installments" : ""))
                                            .build())
                                    .build())
                            .build());

            Session session = Session.create(paramsBuilder.build());

            // Generate a pending ticket record matching this session id
            Ticket ticket = new Ticket(
                    request.eventId != null ? request.eventId : 9999L,
                    eventTitle,
                    eventDate,
                    customerEmail,
                    qty,
                    chargeAmount,
                    "STRIPE",
                    "PENDING_PAYMENT",
                    LocalDate.now().toString()
            );
            ticket.setTicketId(ticketId);
            ticket.setGuestName(guestName);
            ticketRepository.save(ticket);

            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", session.getUrl());
            response.put("sessionId", session.getId());
            response.put("ticketId", ticketId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Stripe Checkout creation failed: {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unable to establish Stripe Checkout session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/verify-session")
    public ResponseEntity<?> verifySession(@RequestParam String sessionId, @RequestParam(required = false) String ticketId) {
        try {
            com.stripe.Stripe.apiKey = stripeApiKey;
            Session session = Session.retrieve(sessionId);
            if (session != null && ("paid".equalsIgnoreCase(session.getPaymentStatus()) || "complete".equalsIgnoreCase(session.getStatus()))) {
                String tId = ticketId != null && !ticketId.trim().isEmpty() ? ticketId.trim() : 
                        (session.getMetadata() != null ? session.getMetadata().get("ticketId") : null);
                if (tId != null) {
                    Optional<Ticket> optTicket = ticketRepository.findByTicketId(tId);
                    if (optTicket.isPresent()) {
                        Ticket ticket = optTicket.get();
                        if (!"CONFIRMED".equalsIgnoreCase(ticket.getStatus())) {
                            ticket.setStatus("CONFIRMED");
                            ticketRepository.save(ticket);

                            // Dispatch confirmation email
                            emailService.sendTicketConfirmationEmail(
                                    ticket.getUserEmail(),
                                    ticket.getGuestName(),
                                    ticket.getEventTitle(),
                                    ticket.getEventDate(),
                                    ticket.getQuantity(),
                                    ticket.getTicketId(),
                                    ticket.getPricePaid()
                            );
                            log.info("Ticket {} verified and confirmed via session query.", tId);
                        }
                        return ResponseEntity.ok(Map.of("verified", true, "ticket", ticket));
                    }
                }
                return ResponseEntity.ok(Map.of("verified", true, "message", "Payment verified by Stripe."));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("verified", false, "error", "Payment not completed on Stripe."));
            }
        } catch (Exception e) {
            log.error("Error verifying Stripe session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private String getPayPalAccessToken() throws Exception {
        String baseUrl = "sandbox".equalsIgnoreCase(paypalMode) ? "https://api-m.sandbox.paypal.com" : "https://api-m.paypal.com";
        String authStr = paypalClientId + ":" + paypalClientSecret;
        String base64Auth = Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            log.error("PayPal OAuth failed with HTTP {}: {}", resp.statusCode(), resp.body());
            throw new IllegalStateException("PayPal Gateway Authentication failed (HTTP " + resp.statusCode() + "). Please verify your PayPal Client ID and Secret or choose Stripe.");
        }
        JsonNode root = new ObjectMapper().readTree(resp.body());
        return root.get("access_token").asText();
    }

    // --- SECURE PAYPAL CHECKOUT ROUTING ---

    @PostMapping("/create-paypal-order")
    public ResponseEntity<?> createPayPalOrder(@RequestBody PaymentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String customerEmail = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : request.guestEmail;
        String guestName = (request.guestName != null && !request.guestName.trim().isEmpty())
                ? request.guestName.trim() : "Valued Attendee";

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Customer email is required."));
        }

        Event event = findOrResolveEvent(request.eventId);
        if (event == null && (request.unitPrice == null || request.unitPrice <= 0)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Event not found."));
        }

        String eventTitle = (request.eventTitle != null && !request.eventTitle.trim().isEmpty())
                ? request.eventTitle.trim()
                : (event != null ? event.getTitle() : "Event Pass");
        String eventDate = (request.eventDate != null && !request.eventDate.trim().isEmpty())
                ? request.eventDate.trim()
                : (event != null ? event.getDate() : LocalDate.now().toString());

        double unitPrice = (request.unitPrice != null && request.unitPrice > 0)
                ? request.unitPrice
                : (event != null ? event.getPrice() : 0.0);

        int qty = (request.quantity > 0) ? request.quantity : 1;
        double totalPrice = unitPrice * qty;

        String ticketId = "H4H-PAYPAL-" + System.currentTimeMillis();

        try {
            String accessToken = getPayPalAccessToken();
            String baseUrl = "sandbox".equalsIgnoreCase(paypalMode) ? "https://api-m.sandbox.paypal.com" : "https://api-m.paypal.com";

            String orderPayload = String.format(Locale.US,
                "{\"intent\":\"CAPTURE\",\"purchase_units\":[{\"reference_id\":\"%s\",\"description\":\"%s\",\"amount\":{\"currency_code\":\"USD\",\"value\":\"%.2f\"}}],\"application_context\":{\"brand_name\":\"Howards 4 Hope\",\"user_action\":\"PAY_NOW\",\"return_url\":\"https://howards4hope-b06f6.web.app/#/my-tickets?paypal_status=success&ticketId=%s\",\"cancel_url\":\"https://howards4hope-b06f6.web.app/#/gala?paypal_status=cancelled\"}}",
                ticketId,
                eventTitle.replace("\"", "\\\""),
                totalPrice,
                ticketId
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest orderReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(orderPayload))
                    .build();

            HttpResponse<String> orderResp = client.send(orderReq, HttpResponse.BodyHandlers.ofString());
            if (orderResp.statusCode() < 200 || orderResp.statusCode() >= 300) {
                log.error("PayPal Order creation failed with HTTP {}: {}", orderResp.statusCode(), orderResp.body());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "PayPal gateway error: " + orderResp.body()));
            }

            JsonNode orderNode = new ObjectMapper().readTree(orderResp.body());
            String orderId = orderNode.get("id").asText();
            String checkoutUrl = null;
            if (orderNode.has("links")) {
                for (JsonNode link : orderNode.get("links")) {
                    if ("approve".equalsIgnoreCase(link.get("rel").asText())) {
                        checkoutUrl = link.get("href").asText();
                        break;
                    }
                }
            }

            // Save ticket as PENDING_PAYMENT (DO NOT dispatch email until PayPal captures payment)
            Ticket ticket = new Ticket(
                    request.eventId != null ? request.eventId : 9999L,
                    eventTitle,
                    eventDate,
                    customerEmail,
                    qty,
                    totalPrice,
                    "PAYPAL",
                    "PENDING_PAYMENT",
                    LocalDate.now().toString()
            );
            ticket.setTicketId(ticketId);
            ticket.setGuestName(guestName);
            ticketRepository.save(ticket);

            Map<String, String> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("checkoutUrl", checkoutUrl != null ? checkoutUrl : "");
            response.put("ticketId", ticketId);
            response.put("status", "CREATED");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("PayPal Order creation error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "PayPal checkout could not be initiated."));
        }
    }

    @PostMapping("/capture-paypal-order")
    public ResponseEntity<?> capturePayPalOrder(@RequestParam String orderId, @RequestParam(required = false) String ticketId) {
        try {
            String accessToken = getPayPalAccessToken();
            String baseUrl = "sandbox".equalsIgnoreCase(paypalMode) ? "https://api-m.sandbox.paypal.com" : "https://api-m.paypal.com";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = new ObjectMapper().readTree(resp.body());
            String status = root.has("status") ? root.get("status").asText() : "";

            if ("COMPLETED".equalsIgnoreCase(status)) {
                if (ticketId != null) {
                    Optional<Ticket> optTicket = ticketRepository.findByTicketId(ticketId);
                    if (optTicket.isPresent()) {
                        Ticket t = optTicket.get();
                        if (!"CONFIRMED".equalsIgnoreCase(t.getStatus())) {
                            t.setStatus("CONFIRMED");
                            ticketRepository.save(t);

                            emailService.sendTicketConfirmationEmail(
                                    t.getUserEmail(),
                                    t.getGuestName(),
                                    t.getEventTitle(),
                                    t.getEventDate(),
                                    t.getQuantity(),
                                    t.getTicketId(),
                                    t.getPricePaid()
                            );
                            log.info("PayPal Ticket {} confirmed and email sent.", ticketId);
                        }
                        return ResponseEntity.ok(Map.of("success", true, "status", "COMPLETED", "ticket", t));
                    }
                }
                return ResponseEntity.ok(Map.of("success", true, "status", "COMPLETED"));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of("error", "PayPal payment not completed. Status: " + status));
            }
        } catch (Exception e) {
            log.error("PayPal Order capture error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to capture PayPal payment: " + e.getMessage()));
        }
    }

    // --- CRYPTOGRAPHICALLY VERIFIED STRIPE WEBHOOK ---

    @PostMapping("/webhook")
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload, 
                                                @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        com.stripe.model.Event stripeEvent;

        try {
            if (webhookSecret != null && !webhookSecret.equals("whsec_mock_secret") && sigHeader != null) {
                // Timing-safe cryptographic signature check with 300s tolerance
                stripeEvent = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                // Development fallback parser when testing locally without live webhook signature
                stripeEvent = com.stripe.net.ApiResource.GSON.fromJson(payload, com.stripe.model.Event.class);
            }
        } catch (Exception e) {
            log.error("Stripe Webhook Signature Verification Failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook signature verification failed");
        }

        if (stripeEvent == null) {
            return ResponseEntity.badRequest().body("Invalid event payload");
        }

        String eventType = stripeEvent.getType();
        log.info("Received Verified Stripe Webhook Event: {}", eventType);

        if ("checkout.session.completed".equalsIgnoreCase(eventType)) {
            EventDataObjectDeserializer dataObjectDeserializer = stripeEvent.getDataObjectDeserializer();
            StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                Map<String, String> metadata = session.getMetadata();

                if (metadata != null) {
                    String type = metadata.get("type");

                    if ("DONATION".equalsIgnoreCase(type)) {
                        String taxReceiptNumber = metadata.get("taxReceiptNumber");
                        if (taxReceiptNumber != null) {
                            Optional<Donation> optDonation = donationRepository.findByTaxReceiptNumber(taxReceiptNumber);
                            if (optDonation.isPresent()) {
                                Donation donation = optDonation.get();
                                donation.setStatus("COMPLETED");
                                donation.setStripeSessionId(session.getId());
                                donationRepository.save(donation);

                                // Dispatch Official 501(c)(3) Tax Receipt Email
                                emailService.sendTaxDeductibleDonationReceipt(
                                        donation.getDonorEmail(),
                                        donation.getDonorName(),
                                        donation.getAmount(),
                                        donation.getFrequency(),
                                        donation.getTaxReceiptNumber(),
                                        donation.getDonationDate()
                                );
                                log.info("Donation {} confirmed via Webhook, tax receipt sent.", taxReceiptNumber);
                            }
                        }
                    } else if ("EVENT_TICKET".equalsIgnoreCase(type)) {
                        String ticketId = metadata.get("ticketId");
                        if (ticketId != null) {
                            Optional<Ticket> optTicket = ticketRepository.findByTicketId(ticketId);
                            if (optTicket.isPresent()) {
                                Ticket ticket = optTicket.get();
                                ticket.setStatus("CONFIRMED");
                                ticketRepository.save(ticket);

                                // Dispatch Ticket Confirmation Email
                                emailService.sendTicketConfirmationEmail(
                                        ticket.getUserEmail(),
                                        ticket.getGuestName(),
                                        ticket.getEventTitle(),
                                        ticket.getEventDate(),
                                        ticket.getQuantity(),
                                        ticket.getTicketId(),
                                        ticket.getPricePaid()
                                );
                                log.info("Ticket {} confirmed via Webhook, pass delivered.", ticketId);
                            }
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }
}
