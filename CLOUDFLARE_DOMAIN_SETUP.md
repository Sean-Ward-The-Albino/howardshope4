# Howards 4 Hope - Domain & Deployment Porting Guide

This guide outlines how to migrate the domain authority from GoDaddy to Cloudflare, connect Firebase Hosting for the frontend, and link the backend to Google Cloud Run.

---

## 1. Domain Migration: GoDaddy to Cloudflare

1. **Add Site to Cloudflare**:
   - Log into [Cloudflare Dashboard](https://dash.cloudflare.com/) and click **Add a Site** -> enter `howards4hope.org`.
   - Select the Free plan. Cloudflare will scan existing DNS records.
2. **Update Nameservers in GoDaddy**:
   - Log into [GoDaddy Domain Portfolio](https://dcc.godaddy.com/control/portfolio).
   - Click `howards4hope.org` -> **DNS** -> **Nameservers** -> **Change Nameservers**.
   - Choose **"I'll use my own nameservers"** and paste Cloudflare's two assigned nameservers (e.g., `alec.ns.cloudflare.com` and `nina.ns.cloudflare.com`).
   - Save changes. DNS propagation takes 5–30 minutes.

---

## 2. Firebase Hosting Custom Domain Setup

1. In the [Firebase Console](https://console.firebase.google.com/project/howards4hope-b06f6/hosting/sites):
   - Click **Add custom domain** -> enter `howards4hope.org` and `www.howards4hope.org`.
2. Firebase will provide two `A` records and an `A` / `CNAME` or `TXT` verification token.
3. In **Cloudflare DNS Management**:
   - Add `A` record: `@` -> Firebase IP 1 (Proxy status: **DNS Only** for SSL issuance).
   - Add `A` record: `@` -> Firebase IP 2 (Proxy status: **DNS Only**).
   - Add `CNAME` record: `www` -> `howards4hope-b06f6.web.app` (Proxy status: **DNS Only**).
   - Once Firebase provisions the SSL certificate (usually 15-30 min), you can toggle Proxy status to **Proxied (Orange Cloud)** for Cloudflare CDN caching and DDoS protection.

---

## 3. Google Cloud Run Backend Deployment

1. **Build & Deploy via Google Cloud CLI**:
   ```bash
   gcloud builds submit --tag gcr.io/howards4hope-b06f6/backend ./backend
   
   gcloud run deploy howards4hope-backend \
     --image gcr.io/howards4hope-b06f6/backend \
     --platform managed \
     --region us-central1 \
     --allow-unauthenticated \
     --set-env-vars="SPRING_PROFILES_ACTIVE=prod,STRIPE_API_KEY=sk_live_...,PAYPAL_CLIENT_ID=..."
   ```
2. Cloud Run assigns a live HTTPS service URL (e.g. `https://howards4hope-backend-xyz.a.run.app`).
3. Update `API.baseUrl` in `frontend/app.js` with your Cloud Run URL or route it through Firebase Hosting rewrites in `firebase.json`:
   ```json
   "rewrites": [
     { "source": "/api/**", "run": { "serviceId": "howards4hope-backend", "region": "us-central1" } },
     { "source": "**", "destination": "/index.html" }
   ]
   ```
