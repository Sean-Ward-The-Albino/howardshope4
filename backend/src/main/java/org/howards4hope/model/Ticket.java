package org.howards4hope.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(unique = true)
    private String ticketId;

    private String guestName;
    private String eventTitle;
    private String eventDate;

    @Column(nullable = false)
    private String userEmail;

    private int quantity;
    private double pricePaid;
    private String paymentMethod;
    private String status; // CONFIRMED, CANCELLED
    private String purchaseDate;

    // Secure Verification Token (prevents guest ticket enumeration)
    private String confirmationToken;

    // Installment Plan & Payment Splitting tracking
    private String paymentPlanType = "FULL"; // "FULL" or "INSTALLMENT"
    private int installmentCycles = 1;
    private int installmentsPaid = 1;
    private double remainingBalance = 0.0;

    // Default constructor
    public Ticket() {
        this.ticketId = "H4H-TKT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        this.confirmationToken = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Convenience constructor
    public Ticket(Long eventId, String eventTitle, String eventDate, String userEmail, int quantity, double pricePaid, String paymentMethod, String status, String purchaseDate) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.userEmail = userEmail;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.confirmationToken = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Ticket(Long eventId, String eventTitle, String eventDate, String userEmail, int quantity, double pricePaid, String paymentMethod, String status, String purchaseDate, String paymentPlanType, int installmentCycles, int installmentsPaid, double remainingBalance) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.userEmail = userEmail;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.paymentPlanType = paymentPlanType;
        this.installmentCycles = installmentCycles;
        this.installmentsPaid = installmentsPaid;
        this.remainingBalance = remainingBalance;
        this.confirmationToken = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(double pricePaid) {
        this.pricePaid = pricePaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public String getPaymentPlanType() {
        return paymentPlanType;
    }

    public void setPaymentPlanType(String paymentPlanType) {
        this.paymentPlanType = paymentPlanType;
    }

    public int getInstallmentCycles() {
        return installmentCycles;
    }

    public void setInstallmentCycles(int installmentCycles) {
        this.installmentCycles = installmentCycles;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
}
