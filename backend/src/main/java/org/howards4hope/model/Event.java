package org.howards4hope.model;

import jakarta.persistence.*;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_events_keyset", columnList = "date DESC, id DESC"),
    @Index(name = "idx_events_category", columnList = "category"),
    @Index(name = "idx_events_date", columnList = "date")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String date; // format: YYYY-MM-DD

    private String time;
    private String location;
    private double price;
    private String bannerUrl;
    private String category;
    private String color;

    // Payment Splitting & Installment Plan Settings
    private boolean allowInstallments = false;
    private Integer installmentCycles = 1; // e.g. 2, 3, 4, 6
    private String installmentFrequency = "Monthly"; // "Monthly", "Bi-Weekly", "Weekly"

    // Default constructor
    public Event() {}

    // Convenience constructor
    public Event(String title, String description, String date, String time, String location, double price, String bannerUrl, String category, String color) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.price = price;
        this.bannerUrl = bannerUrl;
        this.category = category;
        this.color = color;
    }

    public Event(String title, String description, String date, String time, String location, double price, String bannerUrl, String category, String color, boolean allowInstallments, Integer installmentCycles, String installmentFrequency) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.price = price;
        this.bannerUrl = bannerUrl;
        this.category = category;
        this.color = color;
        this.allowInstallments = allowInstallments;
        this.installmentCycles = installmentCycles;
        this.installmentFrequency = installmentFrequency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isAllowInstallments() {
        return allowInstallments;
    }

    public void setAllowInstallments(boolean allowInstallments) {
        this.allowInstallments = allowInstallments;
    }

    public Integer getInstallmentCycles() {
        return installmentCycles;
    }

    public void setInstallmentCycles(Integer installmentCycles) {
        this.installmentCycles = installmentCycles;
    }

    public String getInstallmentFrequency() {
        return installmentFrequency;
    }

    public void setInstallmentFrequency(String installmentFrequency) {
        this.installmentFrequency = installmentFrequency;
    }
}
