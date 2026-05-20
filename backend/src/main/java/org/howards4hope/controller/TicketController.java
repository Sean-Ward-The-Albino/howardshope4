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

    // Request payload structure for booking
    public static class TicketRequest {
        public Long eventId;
        public int quantity;
        public String paymentMethod;
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
        return ResponseEntity.ok(savedTicket);
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
