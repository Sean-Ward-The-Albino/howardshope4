package org.howards4hope.controller;

import org.howards4hope.model.VolunteerApplication;
import org.howards4hope.repository.VolunteerRepository;
import org.howards4hope.service.EmailService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OutreachController {

    private final VolunteerRepository volunteerRepository;
    private final EmailService emailService;

    public OutreachController(VolunteerRepository volunteerRepository, EmailService emailService) {
        this.volunteerRepository = volunteerRepository;
        this.emailService = emailService;
    }

    /**
     * Public endpoint to submit a volunteer, mentor, or community partner application.
     */
    @PostMapping("/outreach/apply")
    public ResponseEntity<?> submitApplication(@RequestBody VolunteerApplication application) {
        if (application.getFullName() == null || application.getFullName().trim().isEmpty() ||
            application.getEmail() == null || application.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Full name and email are required.");
        }

        if (application.getRoleInterest() == null || application.getRoleInterest().trim().isEmpty()) {
            application.setRoleInterest("Volunteer");
        }

        VolunteerApplication saved = volunteerRepository.save(application);

        // Async email dispatch to organization and applicant
        new Thread(() -> {
            try {
                emailService.sendVolunteerApplicationNotification(saved);
                emailService.sendVolunteerAcknowledgmentEmail(saved);
            } catch (Exception e) {
                System.err.println("Failed sending volunteer application notification emails: " + e.getMessage());
            }
        }).start();

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Admin endpoint: retrieve all volunteer & partner applications.
     */
    @GetMapping("/admin/outreach/applications")
    public ResponseEntity<List<VolunteerApplication>> getAllApplications(
            @RequestParam(required = false) String status) {
        if (status != null && !status.trim().isEmpty()) {
            return ResponseEntity.ok(volunteerRepository.findByStatusOrderByAppliedAtDesc(status.toUpperCase().trim()));
        }
        return ResponseEntity.ok(volunteerRepository.findAllByOrderByAppliedAtDesc());
    }

    /**
     * Admin endpoint: update status of a volunteer application.
     */
    @PatchMapping("/admin/outreach/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload) {
        Optional<VolunteerApplication> opt = volunteerRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required.");
        }

        VolunteerApplication app = opt.get();
        app.setStatus(newStatus.toUpperCase().trim());
        volunteerRepository.save(app);

        return ResponseEntity.ok(app);
    }

    /**
     * Admin endpoint: export all volunteer and partner applications to CSV.
     */
    @GetMapping("/admin/outreach/export")
    public ResponseEntity<byte[]> exportApplicationsToCsv() {
        List<VolunteerApplication> apps = volunteerRepository.findAllByOrderByAppliedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Full Name,Email,Phone,Interest Area,Availability,Status,Applied At,Message\n");

        for (VolunteerApplication app : apps) {
            csv.append(app.getId()).append(",")
               .append(escapeCSVField(app.getFullName())).append(",")
               .append(escapeCSVField(app.getEmail())).append(",")
               .append(escapeCSVField(app.getPhone())).append(",")
               .append(escapeCSVField(app.getRoleInterest())).append(",")
               .append(escapeCSVField(app.getAvailability())).append(",")
               .append(escapeCSVField(app.getStatus())).append(",")
               .append(escapeCSVField(app.getAppliedAt() != null ? app.getAppliedAt().toString() : "")).append(",")
               .append(escapeCSVField(app.getMessage())).append("\n");
        }

        if (apps.isEmpty()) {
            csv.append("# No applications recorded yet.\n");
        }

        byte[] csvBytes = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "howards4hope_volunteer_applications.csv");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
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
