package org.howards4hope.controller;

import org.howards4hope.model.Event;
import org.howards4hope.model.Ticket;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private org.howards4hope.service.EmailService emailService;

    // Request payload structure for booking
    public static class TicketRequest {
        public Long eventId;
        public int quantity;
        public String paymentMethod;
    }

    public static class GuestTicketRequest {
        public Long eventId;
        public int quantity;
        public String paymentMethod;
        public String guestEmail;
        public String guestName;
    }

    // --- SECURED TICKETING ENDPOINTS ---

    @PostMapping("/tickets/book")
    public ResponseEntity<?> bookTicket(@RequestBody TicketRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        
        String userEmail = auth.getName(); // Extracts email from Firebase JWT security token
        
        Optional<Event> optionalEvent = eventRepository.findById(request.eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }

        Event event = optionalEvent.get();
        
        // Verify payment validation requirements
        if (event.getPrice() > 0 && "FREE".equalsIgnoreCase(request.paymentMethod)) {
            return ResponseEntity.badRequest().body("This is a paid event. Payment method cannot be FREE.");
        }

        double pricePaid = event.getPrice() * request.quantity;
        String ticketId = "H4H-MEMBER-" + System.currentTimeMillis();
        
        Ticket ticket = new Ticket(
                request.eventId,
                event.getTitle(),
                event.getDate(),
                userEmail,
                request.quantity,
                pricePaid,
                request.paymentMethod,
                "CONFIRMED",
                LocalDate.now().toString()
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        // Dispatch Email confirmation
        emailService.sendTicketConfirmationEmail(
            userEmail,
            userEmail.split("@")[0], // Simple fallback for name
            event.getTitle(),
            event.getDate(),
            request.quantity,
            ticketId,
            pricePaid
        );

        return ResponseEntity.ok(savedTicket);
    }

    // --- PUBLIC TICKETING ENDPOINTS FOR GUEST REGISTRATION ---

    @PostMapping("/tickets/book-guest")
    public ResponseEntity<?> bookTicketGuest(@RequestBody GuestTicketRequest request) {
        if (request.guestEmail == null || request.guestEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Guest email is required.");
        }
        if (request.guestName == null || request.guestName.isEmpty()) {
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

        double pricePaid = event.getPrice() * request.quantity;
        String ticketId = "H4H-GUEST-" + System.currentTimeMillis();

        Ticket ticket = new Ticket(
                request.eventId,
                event.getTitle(),
                event.getDate(),
                request.guestEmail,
                request.quantity,
                pricePaid,
                request.paymentMethod,
                "CONFIRMED",
                LocalDate.now().toString()
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        // Dispatch Email confirmation
        emailService.sendTicketConfirmationEmail(
            request.guestEmail,
            request.guestName,
            event.getTitle(),
            event.getDate(),
            request.quantity,
            ticketId,
            pricePaid
        );

        return ResponseEntity.ok(savedTicket);
    }

    @GetMapping("/tickets/guest-tickets")
    public ResponseEntity<?> getGuestTickets(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email query parameter is required.");
        }
        List<Ticket> tickets = ticketRepository.findByUserEmailIgnoreCase(email);
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

    // --- ADMIN ACCESS ONLY: GET EVENT ATTENDEES ---
    
    @GetMapping("/admin/tickets/attendees/{eventId}")
    public ResponseEntity<List<Ticket>> getEventAttendees(@PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.notFound().build();
        }
        List<Ticket> attendees = ticketRepository.findByEventId(eventId);
        return ResponseEntity.ok(attendees);
    }
}
