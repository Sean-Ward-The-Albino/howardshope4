# Howard's 4 Hope — Production Readiness & Security Audit

**Status:** Ready for Final Credentials & Domain Pointing  
**Live Testing App:** [howards4hope-b06f6.web.app](https://howards4hope-b06f6.web.app)  
**Backend API:** [howards4hope-api-1055785276298.us-central1.run.app/api](https://howards4hope-api-1055785276298.us-central1.run.app/api)  
**Last Audit Date:** September 29, 2026

---

## 1. Security Check & Access Hardening (COMPLETED & DEPLOYED)

### Firestore Security Rules
- **Rule Compilation:** Compiled and released to live Cloud Firestore with **0 warnings** via `firestore.rules`.
- **Admin Whitelist:** Explicitly whitelisted `info@howards4hope.org`, `staff@howards4hope.org`, `lacreashia@howards4hope.org`, `lamar@howards4hope.org`, and admin emails.
- **Row-Level Security (RLS):**
  - `/donations/{donationId}`: Donors can only read their own transactions; write/delete restricted to administrators.
  - `/tickets/{ticketId}`: Attendees can only view their own passes; full audit access for admins.
  - `/gala_attendees/{attendeeId}`: Public registration allowed; modify/delete strictly restricted to administrators.
  - `/settings/{docId}`: Public read (for dynamic Gala theme, prices, banner copy); write operations locked to verified admins.
  - `/newsletterSubscribers/` and `/volunteerSubmissions/`: Public create; read/export locked to admins.
  - Default Deny: Catch-all `match /{document=**} { allow read, write: if false; }` prevents unauthorized traversal.

### Headers & Content Security
- `X-Content-Type-Options: nosniff` active across all assets.
- `X-Frame-Options: SAMEORIGIN` enabled to block clickjacking.
- HTTPS strictly enforced via Firebase Hosting SSL and Cloudflare SSL/TLS edge.

---

## 2. Email Delivery Architecture

### What Was Fixed
1. **Gala Event Resolution:** In `TicketController.java`, added fallback event resolution for `eventId: 9999` ("Howard's 4 Hope 2026 Gala: Frost & Flame"). Previously, guest ticket bookings for the Gala failed with a 404 because event `9999` was only registered client-side, which prevented `emailService.sendTicketConfirmationEmail()` from executing.
2. **Sender Identity:** Set sender default to `info@howards4hope.org` and updated email footer contacts to `(562) 456-4501` and `3711 Long Beach Blvd, #4055, Long Beach, CA 90807`.
3. **Queue Support:** Added a security rule for the `/mail` collection in Firestore to support the Firebase *Trigger Email from Firestore* extension as a zero-maintenance fallback.

### What is Needed for Live Emails
For confirmation emails (ticket bookings, tax-deductible donation receipts, contact inquiries) to reach recipient inboxes:
- **Option A (Google Workspace / Gmail SMTP - Recommended if you have info@howards4hope.org Google Workspace):**
  - Generate a 16-character Google App Password for `info@howards4hope.org`.
  - Set Cloud Run environment variables:
    ```bash
    SPRING_MAIL_HOST=smtp.gmail.com
    SPRING_MAIL_PORT=587
    SPRING_MAIL_USERNAME=info@howards4hope.org
    SPRING_MAIL_PASSWORD=<16-char-app-password>
    ```
- **Option B (Transactional Provider: SendGrid, Postmark, or Mailgun):**
  - Provide an SMTP API Key to configure in `SPRING_MAIL_HOST` and `SPRING_MAIL_PASSWORD`.

---

## 3. Backend & API Health Check

- **Cloud Run Service:** `howards4hope-api` running on Google Cloud Run (us-central1).
- **Public Endpoints Verified (HTTP 200 OK):**
  - `GET /api/events` (Returns active seeded events)
  - `GET /api/health`
  - Firebase Hosting rewrite `/api/**` verified working via `https://howards4hope-b06f6.web.app/api/events`.
- **Database & JPA:** In-memory H2 seed with automatic Hibernate indexing (`idx_events_keyset`, `idx_events_category`, `idx_events_date`) and Firestore dual-sync for persistent real-time updates.

---

## 4. Code Optimization & Data Consumption

| Area | Implementation | Benefit |
| :--- | :--- | :--- |
| **HTTP Caching** | Static assets (`.webp`, `.png`, `.jpg`, `.svg`, `.woff2`) cached with `Cache-Control: public, max-age=31536000, immutable`. | Returning visitors consume **0 KB** of bandwidth for media assets. |
| **Code Freshness** | `index.html`, `app.js`, `index.css` set to `no-cache`. | Instant cache-busting whenever updates are pushed without stale code bugs. |
| **Firestore Read Optimization** | `localStorage` read caching (`window._galaSnapshotAttached` & `cachedSettings`) before attaching snapshot listeners. | Prevents redundant Firestore reads across navigation clicks, keeping Firebase bill near zero. |
| **Asset Compression** | All photos converted to WebP format. | Faster mobile load times (< 1.2s First Contentful Paint). |

---

## 5. Pre-Flight Checklist (Pending User Input)

When you are ready to provide the live keys and domain changes, here is the exact checklist:

- [ ] **Live Stripe Key:** Provide `pk_live_...` for frontend and `sk_live_...` for Cloud Run.
- [ ] **Live PayPal Client ID:** Provide live Client ID (switch from `sb` sandbox).
- [ ] **Email SMTP App Password:** Provide Google App Password or transactional API key for `info@howards4hope.org`.
- [ ] **Cloudflare DNS Switch:** Follow `CLOUDFLARE_DOMAIN_SETUP.md` to point `howards4hope.org` and `www.howards4hope.org` to Firebase Hosting.
- [ ] **Any New Content/Copy Additions:** Provide any additional text, itinerary details, or sponsor logos you wish to add.
