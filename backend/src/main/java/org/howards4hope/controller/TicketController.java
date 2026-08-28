package org.howards4hope.controller;

import org.howards4hope.model.Event;
import org.howards4hope.model.Ticket;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.repository.TicketRepository;
import org.howards4hope.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;

    public TicketController(TicketRepository ticketRepository, EventRepository eventRepository, EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
    }

    public static class TicketRequest {
        public Long eventId;
        public int quantity = 1;
        public String paymentMethod = "FREE";
        public String paymentPlanType = "FULL"; // "FULL" or "INSTALLMENT"
        public int installmentCycles = 1;
    }

    public static class GuestTicketRequest {
        public Long eventId;
        public int quantity = 1;
        public String paymentMethod = "FREE";
        public String guestEmail;
        public String guestName;
        public String paymentPlanType = "FULL"; // "FULL" or "INSTALLMENT"
        public int installmentCycles = 1;
    }

    // --- SECURED TICKETING ENDPOINTS ---

    @PostMapping("/tickets/book")
    public ResponseEntity<?> bookTicket(@RequestBody TicketRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        
        String userEmail = auth.getName();
        
        Optional<Event> optionalEvent = eventRepository.findById(request.eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }

        Event event = optionalEvent.get();
        
        if (event.getPrice() > 0 && "FREE".equalsIgnoreCase(request.paymentMethod)) {
            return ResponseEntity.badRequest().body("This is a paid event. Payment method cannot be FREE.");
        }

        double totalPrice = event.getPrice() * request.quantity;
        boolean isInstallment = "INSTALLMENT".equalsIgnoreCase(request.paymentPlanType) && request.installmentCycles > 1;
        int cycles = isInstallment ? request.installmentCycles : 1;
        double firstPayment = isInstallment ? (totalPrice / cycles) : totalPrice;
        double remainingBalance = isInstallment ? (totalPrice - firstPayment) : 0.0;

        Ticket ticket = new Ticket(
                request.eventId,
                event.getTitle(),
                event.getDate(),
                userEmail,
                request.quantity,
                firstPayment,
                request.paymentMethod,
                "CONFIRMED",
                LocalDate.now().toString(),
                isInstallment ? "INSTALLMENT" : "FULL",
                cycles,
                1,
                remainingBalance
        );
        ticket.setGuestName(userEmail.split("@")[0]);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Dispatch Email confirmation
        emailService.sendTicketConfirmationEmail(
            userEmail,
            savedTicket.getGuestName(),
            event.getTitle(),
            event.getDate(),
            request.quantity,
            savedTicket.getTicketId(),
            firstPayment
        );

        return ResponseEntity.ok(savedTicket);
    }

    // --- PUBLIC TICKETING ENDPOINTS FOR GUEST REGISTRATION ---

    @PostMapping("/tickets/book-guest")
    public ResponseEntity<?> bookTicketGuest(@RequestBody GuestTicketRequest request) {
        if (request.guestEmail == null || request.guestEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Guest email is required.");
        }
        if (request.guestName == null || request.guestName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Guest name is required.");
        }

        Optional<Event> optionalEvent = eventRepository.findById(request.eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }

        Event event = optionalEvent.get();

        if (event.getPrice() > 0 && "FREE".equalsIgnoreCase(request.paymentMethod)) {
            return ResponseEntity.badRequest().body("This is a paid event. Payment method cannot be FREE.");
        }

        double totalPrice = event.getPrice() * request.quantity;
        boolean isInstallment = "INSTALLMENT".equalsIgnoreCase(request.paymentPlanType) && request.installmentCycles > 1;
        int cycles = isInstallment ? request.installmentCycles : 1;
        double firstPayment = isInstallment ? (totalPrice / cycles) : totalPrice;
        double remainingBalance = isInstallment ? (totalPrice - firstPayment) : 0.0;

        Ticket ticket = new Ticket(
                request.eventId,
                event.getTitle(),
                event.getDate(),
                request.guestEmail.trim(),
                request.quantity,
                firstPayment,
                request.paymentMethod,
                "CONFIRMED",
                LocalDate.now().toString(),
                isInstallment ? "INSTALLMENT" : "FULL",
                cycles,
                1,
                remainingBalance
        );
        ticket.setGuestName(request.guestName.trim());

        Ticket savedTicket = ticketRepository.save(ticket);

        // Dispatch Email confirmation
        emailService.sendTicketConfirmationEmail(
            savedTicket.getUserEmail(),
            savedTicket.getGuestName(),
            event.getTitle(),
            event.getDate(),
            request.quantity,
            savedTicket.getTicketId(),
            firstPayment
        );

        return ResponseEntity.ok(savedTicket);
    }

    /**
     * Secure Guest Ticket Lookup (no sign-in required).
     * Validates via unique ticketId, confirmationToken, or email.
     */
    @GetMapping("/tickets/lookup")
    public ResponseEntity<?> lookupTicket(
            @RequestParam(required = false) String ticketId,
            @RequestParam(required = false) String confirmationToken,
            @RequestParam(required = false) String email) {
        
        if (ticketId != null && !ticketId.trim().isEmpty()) {
            Optional<Ticket> opt = ticketRepository.findByTicketId(ticketId.trim());
            if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        }

        if (confirmationToken != null && !confirmationToken.trim().isEmpty()) {
            Optional<Ticket> opt = ticketRepository.findByConfirmationToken(confirmationToken.trim());
            if (opt.isPresent()) return ResponseEntity.ok(opt.get());
        }

        if (email != null && !email.trim().isEmpty()) {
            List<Ticket> tickets = ticketRepository.findByUserEmailIgnoreCase(email.trim());
            if (!tickets.isEmpty()) return ResponseEntity.ok(tickets);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching ticket found with provided credentials.");
    }

    @GetMapping("/tickets/guest-tickets")
    public ResponseEntity<?> getGuestTickets(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email query parameter is required.");
        }
        List<Ticket> tickets = ticketRepository.findByUserEmailIgnoreCase(email.trim());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets/my-tickets")
    public ResponseEntity<List<Ticket>> getMyTickets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String userEmail = auth.getName();
        List<Ticket> tickets = ticketRepository.findByUserEmailIgnoreCase(userEmail);
        return ResponseEntity.ok(tickets);
    }

    // --- ADMIN ACCESS ONLY: GET EVENT RSVP ATTENDEES LIST ---
    
    @GetMapping("/admin/tickets/attendees/{eventId}")
    public ResponseEntity<List<Ticket>> getEventAttendees(@PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.notFound().build();
        }
        List<Ticket> attendees = ticketRepository.findByEventId(eventId);
        return ResponseEntity.ok(attendees);
    }
}
