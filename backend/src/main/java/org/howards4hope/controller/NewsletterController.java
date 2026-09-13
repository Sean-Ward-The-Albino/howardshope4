package org.howards4hope.controller;

import org.howards4hope.model.NewsletterSubscriber;
import org.howards4hope.repository.NewsletterRepository;
import org.howards4hope.service.EmailService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class NewsletterController {

    private final NewsletterRepository newsletterRepository;
    private final EmailService emailService;

    public NewsletterController(NewsletterRepository newsletterRepository, EmailService emailService) {
        this.newsletterRepository = newsletterRepository;
        this.emailService = emailService;
    }

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        String cleanEmail = email.trim().toLowerCase();
        Optional<NewsletterSubscriber> existing = newsletterRepository.findByEmail(cleanEmail);
        if (existing.isPresent()) {
            return ResponseEntity.ok().body("Already subscribed");
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber(cleanEmail);
        newsletterRepository.save(subscriber);

        // Dispatch welcome confirmation email
        emailService.sendNewsletterWelcomeEmail(cleanEmail);

        return ResponseEntity.ok().body("Successfully subscribed");
    }

    @PostMapping("/newsletter/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email != null) {
            Optional<NewsletterSubscriber> existing = newsletterRepository.findByEmail(email.trim().toLowerCase());
            existing.ifPresent(newsletterRepository::delete);
        }
        return ResponseEntity.ok().body("Successfully unsubscribed");
    }

    @GetMapping("/admin/newsletter/export")
    public ResponseEntity<String> exportCsv() {
        List<NewsletterSubscriber> subscribers = newsletterRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Email,SubscribedAt\n");
        
        for (NewsletterSubscriber sub : subscribers) {
            csv.append(sub.getId()).append(",")
               .append(escapeCSVField(sub.getEmail())).append(",")
               .append(escapeCSVField(sub.getSubscribedAt() != null ? sub.getSubscribedAt().toString() : "")).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=newsletter_subscribers.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }
        String sanitized = field;
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
