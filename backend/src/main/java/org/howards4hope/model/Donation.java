package org.howards4hope.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 501(c)(3) Tax-Deductible Donation Entity.
 * Records charitable gifts, recurring pledges/plans, and generates official IRS Section 170 tax receipts.
 */
@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donations_receipt", columnList = "taxReceiptNumber"),
    @Index(name = "idx_donations_email", columnList = "donorEmail"),
    @Index(name = "idx_donations_date", columnList = "donationDate")
})
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String donorName;

    @Column(nullable = false)
    private String donorEmail;

    @Column(nullable = false)
    private double amount;

    private String frequency = "ONE_TIME"; // "ONE_TIME", "MONTHLY", "QUARTERLY", "ANNUAL"
    private String paymentMethod = "STRIPE"; // "STRIPE", "PAYPAL"
    private String status = "COMPLETED"; // "COMPLETED", "ACTIVE_SUBSCRIPTION", "CANCELLED"
    
    @Column(unique = true, nullable = false)
    private String taxReceiptNumber;

    private String ein = "86-1910919";
    private String donationDate;
    private String stripeSessionId;
    private String stripeSubscriptionId;

    public Donation() {
        this.donationDate = LocalDate.now().toString();
        this.taxReceiptNumber = "H4H-TAX-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Donation(String donorName, String donorEmail, double amount, String frequency, String paymentMethod, String status) {
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.amount = amount;
        this.frequency = frequency != null ? frequency : "ONE_TIME";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "STRIPE";
        this.status = status != null ? status : "COMPLETED";
        this.donationDate = LocalDate.now().toString();
        this.taxReceiptNumber = "H4H-TAX-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorEmail() {
        return donorEmail;
    }

    public void setDonorEmail(String donorEmail) {
        this.donorEmail = donorEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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

    public String getTaxReceiptNumber() {
        return taxReceiptNumber;
    }

    public void setTaxReceiptNumber(String taxReceiptNumber) {
        this.taxReceiptNumber = taxReceiptNumber;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(String donationDate) {
        this.donationDate = donationDate;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }
}
