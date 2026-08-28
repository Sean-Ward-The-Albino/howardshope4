package org.howards4hope.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
public class PageAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String visitorId;

    public PageAnalytics() {}

    public PageAnalytics(String path, String visitorId) {
        this.path = path;
        this.visitorId = visitorId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getVisitorId() { return visitorId; }
    public void setVisitorId(String visitorId) { this.visitorId = visitorId; }
}
