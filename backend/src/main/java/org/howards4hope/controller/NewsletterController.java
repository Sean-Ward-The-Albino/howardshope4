package org.howards4hope.controller;

import org.howards4hope.model.NewsletterSubscriber;
import org.howards4hope.repository.NewsletterRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NewsletterController {

    private final NewsletterRepository newsletterRepository;

    public NewsletterController(NewsletterRepository newsletterRepository) {
        this.newsletterRepository = newsletterRepository;
    }

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        Optional<NewsletterSubscriber> existing = newsletterRepository.findByEmail(email);
        if (existing.isPresent()) {
            return ResponseEntity.ok().body("Already subscribed");
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber(email);
        newsletterRepository.save(subscriber);
        return ResponseEntity.ok().body("Successfully subscribed");
    }

    @GetMapping("/admin/newsletter/export")
    public ResponseEntity<String> exportCsv() {
        List<NewsletterSubscriber> subscribers = newsletterRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Email,SubscribedAt\n");
        
        for (NewsletterSubscriber sub : subscribers) {
            csv.append(sub.getId()).append(",")
               .append(sub.getEmail()).append(",")
               .append(sub.getSubscribedAt()).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=newsletter_subscribers.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }
}
