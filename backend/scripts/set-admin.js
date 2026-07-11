#!/usr/bin/env node

/**
 * ============================================================================
 * Howards 4 Hope — Firebase Admin Claims Setup Script
 * ============================================================================
 *
 * This script sets the `admin: true` custom claim on a Firebase Auth user,
 * enabling secure role-based access control (RBAC) without hardcoding emails.
 *
 * PREREQUISITES:
 *   1. Node.js 18+ installed
 *   2. Install firebase-admin:  npm install firebase-admin
 *   3. Place your firebase-service-account.json in:
 *      backend/src/main/resources/firebase-service-account.json
 *
 * USAGE:
 *   node set-admin.js <email>          — Grant admin role
 *   node set-admin.js <email> --revoke — Revoke admin role
 *
 * EXAMPLES:
 *   node set-admin.js avlorycorp@gmail.com
 *   node set-admin.js volunteer@howards4hope.org --revoke
 *
 * IMPORTANT:
 *   After setting claims, the user must sign out and sign back in
 *   (or wait up to 1 hour) for the new claims to take effect.
 * ============================================================================
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const path = require('path');

// ---- Configuration ----
const SERVICE_ACCOUNT_PATH = path.resolve(
  __dirname,
  '../src/main/resources/firebase-service-account.json'
);

// ---- Argument Parsing ----
const args = process.argv.slice(2);
const email = args[0];
const revoke = args.includes('--revoke');

if (!email || email.startsWith('--')) {
  console.error('');
  console.error('  Usage:  node set-admin.js <email> [--revoke]');
  console.error('');
  console.error('  Examples:');
  console.error('    node set-admin.js avlorycorp@gmail.com');
  console.error('    node set-admin.js user@example.com --revoke');
  console.error('');
  process.exit(1);
}

// ---- Initialize Firebase Admin SDK ----
try {
  const serviceAccount = require(SERVICE_ACCOUNT_PATH);
  initializeApp({
    credential: cert(serviceAccount),
  });
} catch (err) {
  console.error('');
  console.error('  ❌ Failed to load Firebase service account credentials.');
  console.error(`     Expected file at: ${SERVICE_ACCOUNT_PATH}`);
  console.error(`     Error details:`, err);
  console.error('');
  console.error('  To fix this:');
  console.error('    1. Go to Firebase Console → Project Settings → Service Accounts');
  console.error('    2. Click "Generate new private key"');
  console.error('    3. Save the file as: backend/src/main/resources/firebase-service-account.json');
  console.error('');
  process.exit(1);
}

// ---- Set or Revoke Admin Custom Claim ----
async function main() {
  try {
    // Look up the user by email
    const user = await getAuth().getUserByEmail(email);
    console.log(`\n  Found user: ${user.email} (UID: ${user.uid})`);
    console.log(`  Current custom claims: ${JSON.stringify(user.customClaims || {})}`);

    if (revoke) {
      // Remove admin claim (preserve other claims if any)
      const existingClaims = user.customClaims || {};
      delete existingClaims.admin;
      await getAuth().setCustomUserClaims(user.uid, existingClaims);
      console.log(`\n  ✅ Admin claim REVOKED for ${email}`);
    } else {
      // Set admin claim (preserve other claims if any)
      const existingClaims = user.customClaims || {};
      existingClaims.admin = true;
      await getAuth().setCustomUserClaims(user.uid, existingClaims);
      console.log(`\n  ✅ Admin claim GRANTED for ${email}`);
    }

    console.log('  ℹ️  The user must sign out and sign back in for the change to take effect.');
    console.log('');
    process.exit(0);
  } catch (err) {
    if (err.code === 'auth/user-not-found') {
      console.error(`\n  ❌ No user found with email: ${email}`);
      console.error('     The user must sign up on the website first before being granted admin.\n');
    } else {
      console.error(`\n  ❌ Error: ${err.message}\n`);
    }
    process.exit(1);
  }
}

main();
