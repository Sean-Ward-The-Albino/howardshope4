package org.howards4hope.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_applications")
public class VolunteerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String roleInterest; // e.g. "Volunteer", "Mentor", "Community Partner", "Caregiver Support", "Sponsor"

    @Column(length = 4000)
    private String message;

    private String availability;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, REVIEWED, CONTACTED, ARCHIVED

    private LocalDateTime appliedAt = LocalDateTime.now();

    public VolunteerApplication() {}

    public VolunteerApplication(String fullName, String email, String phone, String roleInterest, String message, String availability) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleInterest = roleInterest;
        this.message = message;
        this.availability = availability;
        this.appliedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoleInterest() {
        return roleInterest;
    }

    public void setRoleInterest(String roleInterest) {
        this.roleInterest = roleInterest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
