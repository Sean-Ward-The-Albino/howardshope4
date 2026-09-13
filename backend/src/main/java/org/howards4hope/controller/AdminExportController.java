package org.howards4hope.controller;

import org.howards4hope.model.Event;
import org.howards4hope.model.Ticket;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.repository.TicketRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminExportController {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public AdminExportController(TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/tickets/export/{eventId}")
    public ResponseEntity<byte[]> exportAttendeesToCSV(@PathVariable Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        // Build the CSV file contents
        StringBuilder csvBuilder = new StringBuilder();
        
        // CSV Header
        csvBuilder.append("Ticket ID,Attendee Email,Quantity,Payment Method,Price Paid ($),Purchase Date,Status\n");
        
        // Append ticket rows
        for (Ticket ticket : tickets) {
            String tId = ticket.getTicketId() != null ? ticket.getTicketId() : ("H4H-TKT-" + ticket.getId());
            csvBuilder.append(escapeCSVField(tId)).append(",")
                      .append(escapeCSVField(ticket.getUserEmail())).append(",")
                      .append(ticket.getQuantity()).append(",")
                      .append(escapeCSVField(ticket.getPaymentMethod())).append(",")
                      .append(String.format("%.2f", ticket.getPricePaid())).append(",")
                      .append(escapeCSVField(ticket.getPurchaseDate())).append(",")
                      .append(escapeCSVField(ticket.getStatus())).append("\n");
        }

        // If tickets is empty, indicate no attendees currently booked
        if (tickets.isEmpty()) {
            csvBuilder.append("# No attendees have registered for this event yet.\n");
        }

        byte[] csvBytes = csvBuilder.toString().getBytes();

        String filename = event.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_Attendees_List.csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }
        String sanitized = field;
        // Prevent CSV Formula Injection (DDE injection on =, +, -, @, tab, cr)
        if (sanitized.startsWith("=") || sanitized.startsWith("+") || sanitized.startsWith("-") ||
            sanitized.startsWith("@") || sanitized.startsWith("\t") || sanitized.startsWith("\r")) {
            sanitized = "'" + sanitized;
        }
        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n") || sanitized.contains("\r")) {
            return "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }
}
