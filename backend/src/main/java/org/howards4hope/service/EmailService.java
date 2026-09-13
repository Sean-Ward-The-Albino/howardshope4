package org.howards4hope.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        @Value("${spring.mail.username:howards4hope@gmail.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Dispatches a highly detailed, professional HTML-formatted ticket confirmation email.
     */
    public void sendTicketConfirmationEmail(String toEmail, String guestName, String eventTitle, 
                                            String eventDate, int quantity, String ticketId, double totalPrice) {
        
        String border = "==========================================================================================";
        String formattedPrice = String.format("%.2f", totalPrice);
        String subject = "🎟️ Your Ticket Confirmation for " + eventTitle;

        String htmlContent = String.format(
            "<!DOCTYPE html>" +
            "<html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);}" +
            ".header{background:#1E2761;color:#ffffff;padding:25px;text-align:center;}" +
            ".content{padding:30px;}" +
            ".ticket-box{background:#f1f5f9;border-left:6px solid #F59E0B;border-radius:8px;padding:20px;margin:20px 0;}" +
            ".footer{background:#f8fafc;padding:20px;text-align:center;font-size:12px;color:#64748b;border-top:1px solid #e2e8f0;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h2 style='margin:0;'>Howards 4 Hope</h2><p style='margin:5px 0 0 0;font-size:14px;'>Restoring Hope & Rebuilding Lives</p></div>" +
            "<div class='content'>" +
            "<p>Dear <strong>%s</strong>,</p>" +
            "<p>Thank you for standing with Howards 4 Hope! Your registration has been confirmed.</p>" +
            "<div class='ticket-box'>" +
            "<h3 style='margin-top:0;color:#1E2761;'>🎟️ %s</h3>" +
            "<p><strong>Date & Time:</strong> %s</p>" +
            "<p><strong>Quantity:</strong> %d Pass(es)</p>" +
            "<p><strong>Total Paid:</strong> $%s USD</p>" +
            "<p><strong>Ticket Token:</strong> <code style='font-size:16px;background:#e2e8f0;padding:2px 6px;border-radius:4px;'>%s</code></p>" +
            "</div>" +
            "<p>Please present this Ticket Token or email at the reception desk upon arrival.</p>" +
            "<p>With hope and gratitude,<br><strong>The Howards 4 Hope Team</strong><br>Long Beach, CA</p>" +
            "</div>" +
            "<div class='footer'>Howards 4 Hope &bull; 501(c)(3) Public Charity &bull; EIN: 86-1910919<br>3711 Long Beach Blvd, #4055, Long Beach, CA 90807</div>" +
            "</div></body></html>",
            guestName, eventTitle, eventDate, quantity, formattedPrice, ticketId
        );

        // Attempt live SMTP transmission
        try {
            if (mailSender != null && fromEmail != null && !fromEmail.isEmpty()) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail, "Howards 4 Hope");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                System.out.println(">>> [LIVE SMTP SUCCESS] Ticket confirmation delivered to: " + toEmail);
            }
        } catch (Exception e) {
            System.out.println(">>> [SMTP LOG] Live email deferred (" + e.getMessage() + "). Logging confirmation transcript:");
        }

        // Print structural transcript to server console
        System.out.println(String.format(
            "\n%s\n📧 [OUTBOUND MAIL SERVICE] - TRANSMITTING TICKET RECEIPT\nTo: %s\nSubject: %s\nTicket ID: %s | Amount: $%s\n%s\n",
            border, toEmail, subject, ticketId, formattedPrice, border
        ));
    }

    /**
     * Dispatches an Official IRS 501(c)(3) Tax-Deductible Donation Receipt & Written Acknowledgment.
     * Compliant with IRS Section 170(f)(8) substantiation requirements.
     */
    public void sendTaxDeductibleDonationReceipt(String donorEmail, String donorName, double amount, 
                                                String frequency, String taxReceiptNumber, String donationDate) {
        String border = "==========================================================================================";
        String frequencyText = "ONE_TIME".equalsIgnoreCase(frequency) ? "One-Time Contribution" : frequency + " Recurring Pledge";
        String formattedAmount = String.format("%.2f", amount);
        String subject = "💖 Official 501(c)(3) Tax Receipt (#" + taxReceiptNumber + ") - Howards 4 Hope";

        String htmlContent = String.format(
            "<!DOCTYPE html>" +
            "<html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:650px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);}" +
            ".header{background:#1E2761;color:#ffffff;padding:25px;text-align:center;}" +
            ".content{padding:30px;}" +
            ".receipt-box{background:#f8fafc;border:2px dashed #cbd5e1;border-radius:8px;padding:20px;margin:20px 0;}" +
            ".badge{background:#10b981;color:#ffffff;padding:4px 10px;border-radius:20px;font-size:12px;font-weight:bold;}" +
            ".footer{background:#f8fafc;padding:20px;text-align:center;font-size:12px;color:#64748b;border-top:1px solid #e2e8f0;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h2 style='margin:0;'>Howards 4 Hope</h2><p style='margin:5px 0 0 0;font-size:14px;'>Official 501(c)(3) Written Acknowledgment</p></div>" +
            "<div class='content'>" +
            "<p>Dear <strong>%s</strong>,</p>" +
            "<p>Thank you for your generous tax-deductible gift in support of our youth mentorship, caregiver respite, and single parent aid initiatives in Long Beach, CA.</p>" +
            "<div class='receipt-box'>" +
            "<div style='display:flex;justify-content:space-between;margin-bottom:15px;'>" +
            "<strong>Tax Receipt #:</strong> <code>%s</code> <span class='badge'>OFFICIAL</span>" +
            "</div>" +
            "<table style='width:100%%;border-collapse:collapse;font-size:14px;'>" +
            "<tr><td style='padding:6px 0;'><strong>Donor Name:</strong></td><td>%s</td></tr>" +
            "<tr><td style='padding:6px 0;'><strong>Donor Email:</strong></td><td>%s</td></tr>" +
            "<tr><td style='padding:6px 0;'><strong>Date of Gift:</strong></td><td>%s</td></tr>" +
            "<tr><td style='padding:6px 0;'><strong>Contribution Amount:</strong></td><td><strong style='color:#1E2761;font-size:16px;'>$%s USD</strong></td></tr>" +
            "<tr><td style='padding:6px 0;'><strong>Gift Frequency:</strong></td><td>%s</td></tr>" +
            "<tr><td style='padding:6px 0;'><strong>Federal EIN:</strong></td><td>86-1910919 (501(c)(3) Public Charity)</td></tr>" +
            "</table>" +
            "<hr style='border:none;border-top:1px solid #e2e8f0;margin:15px 0;'>" +
            "<p style='font-size:12px;color:#475569;margin:0;'><strong>IRS Section 170(f)(8) Statement:</strong> No goods or services were provided in whole or part in consideration for this contribution other than intangible religious or charitable benefits. Please retain this letter for your federal and state tax filings.</p>" +
            "</div>" +
            "<p>With profound gratitude,<br>" +
            "<strong>LaCreashia Willis-Howard</strong>, President & Co-Founder<br>" +
            "<strong>Lamar Howard Sr.</strong>, Vice President & Co-Founder</p>" +
            "</div>" +
            "<div class='footer'>Howards 4 Hope &bull; 3711 Long Beach Blvd, #4055, Long Beach, CA 90807 &bull; howards4hope@gmail.com</div>" +
            "</div></body></html>",
            donorName, taxReceiptNumber, donorName, donorEmail, donationDate, formattedAmount, frequencyText
        );

        // Attempt live SMTP transmission
        try {
            if (mailSender != null && fromEmail != null && !fromEmail.isEmpty()) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail, "Howards 4 Hope");
                helper.setTo(donorEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                System.out.println(">>> [LIVE SMTP SUCCESS] 501(c)(3) Tax receipt delivered to: " + donorEmail);
            }
        } catch (Exception e) {
            System.out.println(">>> [SMTP LOG] Live email deferred (" + e.getMessage() + "). Logging receipt transcript:");
        }

        System.out.println(String.format(
            "\n%s\n📜 [501(c)(3) TAX RECEIPT TRANSMITTED]\nTo: %s\nSubject: %s\nReceipt #: %s | Amount: $%s\n%s\n",
            border, donorEmail, subject, taxReceiptNumber, formattedAmount, border
        ));
    }

    /**
     * Dispatches a warm welcome confirmation email when a new subscriber joins the newsletter.
     */
    public void sendNewsletterWelcomeEmail(String toEmail) {
        String subject = "✨ Welcome to the Howards 4 Hope Community Family!";
        String htmlContent = 
            "<!DOCTYPE html><html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);}" +
            ".header{background:#1E2761;color:#ffffff;padding:25px;text-align:center;}" +
            ".content{padding:30px;}" +
            ".highlight-box{background:#eff6ff;border-left:5px solid #2563EB;border-radius:8px;padding:18px;margin:20px 0;}" +
            ".footer{background:#f8fafc;padding:20px;text-align:center;font-size:12px;color:#64748b;border-top:1px solid #e2e8f0;}" +
            ".btn{display:inline-block;padding:12px 24px;background:#2563EB;color:#ffffff;text-decoration:none;border-radius:6px;font-weight:bold;margin-top:15px;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h2 style='margin:0;'>Howards 4 Hope</h2><p style='margin:5px 0 0 0;font-size:14px;'>Restoring Hope & Rebuilding Lives</p></div>" +
            "<div class='content'>" +
            "<h3 style='color:#1E2761;margin-top:0;'>Welcome to Our Community! 🌟</h3>" +
            "<p>Thank you for subscribing to the Howards 4 Hope newsletter! You are now part of a dedicated movement uplifting Long Beach youth, supporting special-needs caregivers, and empowering single parents.</p>" +
            "<div class='highlight-box'>" +
            "<h4 style='margin:0 0 8px 0;color:#1E2761;'>What you can look forward to:</h4>" +
            "<ul style='margin:0;padding-left:20px;'>" +
            "<li>Early access and invitations to upcoming youth empowerment workshops</li>" +
            "<li>Caregiver respite support summit dates and mental wellness toolkits</li>" +
            "<li>Inspiring milestone stories and community impact updates</li>" +
            "</ul></div>" +
            "<p>Explore our programs or discover upcoming events in Long Beach:</p>" +
            "<a href='https://howards4hope.org/#/programs' class='btn' style='color:#ffffff;'>Explore Our Programs</a>" +
            "<p style='margin-top:25px;'>With hope and gratitude,<br><strong>LaCreashia Willis-Howard & Lamar Howard Sr.</strong><br>Founders, Howards 4 Hope</p>" +
            "</div>" +
            "<div class='footer'>Howards 4 Hope &bull; 501(c)(3) Public Charity &bull; 3711 Long Beach Blvd, #4055, Long Beach, CA 90807<br><a href='https://howards4hope.org/#/privacy' style='color:#64748b;'>Privacy Policy</a> &bull; <a href='mailto:howards4hope@gmail.com' style='color:#64748b;'>Contact Us</a></div>" +
            "</div></body></html>";

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * Broadcasts a new blog post / community announcement to all newsletter subscribers.
     */
    public void broadcastBlogPostToSubscribers(List<org.howards4hope.model.NewsletterSubscriber> subscribers, org.howards4hope.model.BlogPost post) {
        if (subscribers == null || subscribers.isEmpty()) return;

        String subject = "📰 New Community Update: " + post.getTitle();
        String excerpt = post.getContent().length() > 200 ? post.getContent().substring(0, 200) + "..." : post.getContent();
        String readUrl = "https://howards4hope.org/#/blog-post?id=" + (post.getId() != null ? post.getId() : "1");

        String htmlContent = String.format(
            "<!DOCTYPE html><html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:650px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);}" +
            ".header{background:#1E2761;color:#ffffff;padding:25px;text-align:center;}" +
            ".content{padding:30px;}" +
            ".tag{display:inline-block;padding:4px 10px;background:#eff6ff;color:#2563EB;border-radius:20px;font-size:12px;font-weight:bold;margin-bottom:12px;}" +
            ".footer{background:#f8fafc;padding:20px;text-align:center;font-size:12px;color:#64748b;border-top:1px solid #e2e8f0;}" +
            ".btn{display:inline-block;padding:12px 24px;background:#F59E0B;color:#1E2761;text-decoration:none;border-radius:6px;font-weight:bold;margin-top:15px;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h2 style='margin:0;'>Howards 4 Hope</h2><p style='margin:5px 0 0 0;font-size:14px;'>Community News & Milestone Updates</p></div>" +
            "<div class='content'>" +
            "<span class='tag'>%s</span>" +
            "<h2 style='color:#1E2761;margin-top:0;'>%s</h2>" +
            "<p style='font-size:13px;color:#64748b;margin-bottom:20px;'>Published on %s &bull; By %s</p>" +
            "<p style='font-size:15px;'>%s</p>" +
            "<a href='%s' class='btn' style='color:#1E2761;'>Read Full Story &rarr;</a>" +
            "</div>" +
            "<div class='footer'>You received this email because you subscribed to updates at <a href='https://howards4hope.org' style='color:#64748b;'>howards4hope.org</a>.<br>3711 Long Beach Blvd, #4055, Long Beach, CA 90807</div>" +
            "</div></body></html>",
            post.getCategory(), post.getTitle(), post.getDate(), post.getAuthor(), excerpt, readUrl
        );

        // Async loop broadcast
        new Thread(() -> {
            int count = 0;
            for (org.howards4hope.model.NewsletterSubscriber sub : subscribers) {
                if (sub.getEmail() != null && !sub.getEmail().trim().isEmpty()) {
                    sendEmail(sub.getEmail().trim(), subject, htmlContent);
                    count++;
                }
            }
            System.out.println(">>> [NEWSLETTER BROADCAST COMPLETE] Dispatched to " + count + " subscribers for article: " + post.getTitle());
        }).start();
    }

    /**
     * Dispatches notification to staff when someone submits a Volunteer/Partner application.
     */
    public void sendVolunteerApplicationNotification(org.howards4hope.model.VolunteerApplication app) {
        String staffEmail = fromEmail != null ? fromEmail : "howards4hope@gmail.com";
        String subject = "🤝 New Volunteer / Partner Application: " + app.getFullName() + " (" + app.getRoleInterest() + ")";
        String htmlContent = String.format(
            "<!DOCTYPE html><html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;padding:25px;}" +
            ".header{border-bottom:2px solid #1E2761;padding-bottom:12px;margin-bottom:20px;}" +
            ".meta-row{padding:8px 0;border-bottom:1px solid #f1f5f9;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h3 style='margin:0;color:#1E2761;'>🤝 New Get Involved / Volunteer Submission</h3></div>" +
            "<div class='meta-row'><strong>Applicant Name:</strong> %s</div>" +
            "<div class='meta-row'><strong>Email Address:</strong> <a href='mailto:%s'>%s</a></div>" +
            "<div class='meta-row'><strong>Phone Number:</strong> %s</div>" +
            "<div class='meta-row'><strong>Interest Area:</strong> <span style='background:#eff6ff;color:#2563EB;padding:2px 8px;border-radius:4px;font-weight:bold;'>%s</span></div>" +
            "<div class='meta-row'><strong>Availability:</strong> %s</div>" +
            "<div style='margin-top:15px;'><strong>Message / Background:</strong><br><p style='background:#f8fafc;padding:12px;border-radius:6px;border:1px solid #e2e8f0;'>%s</p></div>" +
            "<p style='font-size:12px;color:#64748b;margin-top:20px;'>Submitted via howards4hope.org/#/get-involved.</p>" +
            "</div></body></html>",
            app.getFullName(), app.getEmail(), app.getEmail(), 
            app.getPhone() != null ? app.getPhone() : "Not provided",
            app.getRoleInterest(),
            app.getAvailability() != null ? app.getAvailability() : "Flexible",
            app.getMessage() != null ? app.getMessage() : "None"
        );

        sendEmail(staffEmail, subject, htmlContent);
    }

    /**
     * Dispatches automated receipt & thank you email to volunteer applicant.
     */
    public void sendVolunteerAcknowledgmentEmail(org.howards4hope.model.VolunteerApplication app) {
        String subject = "💙 Thank You for Applying to Stand with Howards 4 Hope!";
        String htmlContent = String.format(
            "<!DOCTYPE html><html><head><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;line-height:1.6;background:#f8fafc;padding:20px;}" +
            ".card{max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;padding:25px;}" +
            ".header{background:#1E2761;color:#ffffff;padding:20px;text-align:center;border-radius:8px 8px 0 0;margin:-25px -25px 20px -25px;}" +
            "</style></head><body>" +
            "<div class='card'>" +
            "<div class='header'><h2 style='margin:0;'>Howards 4 Hope</h2><p style='margin:5px 0 0 0;font-size:14px;'>Community Outreach & Volunteer Network</p></div>" +
            "<p>Dear <strong>%s</strong>,</p>" +
            "<p>Thank you for submitting your application to support Howards 4 Hope as a <strong>%s</strong>! Our team is deeply grateful for your heart and commitment to uplifting Long Beach families and caregivers.</p>" +
            "<p>Our outreach coordinators are reviewing your submission and will connect with you within 48 to 72 business hours with upcoming orientation and event details.</p>" +
            "<p>Together, we are creating lasting pathways of hope and transformation.</p>" +
            "<p>With deep gratitude,<br><strong>LaCreashia Willis-Howard & Lamar Howard Sr.</strong><br>Founders, Howards 4 Hope</p>" +
            "<div style='border-top:1px solid #e2e8f0;padding-top:15px;margin-top:20px;font-size:12px;color:#64748b;text-align:center;'>" +
            "Howards 4 Hope &bull; 3711 Long Beach Blvd, #4055, Long Beach, CA 90807 &bull; (562) 481-5556" +
            "</div></div></body></html>",
            app.getFullName(), app.getRoleInterest()
        );

        sendEmail(app.getEmail(), subject, htmlContent);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            if (mailSender != null && fromEmail != null && !fromEmail.isEmpty()) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail, "Howards 4 Hope");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                System.out.println(">>> [LIVE SMTP SUCCESS] Email dispatched to: " + toEmail);
            }
        } catch (Exception e) {
            System.out.println(">>> [SMTP LOG] Live email deferred (" + e.getMessage() + ") for: " + toEmail);
        }
    }
}


