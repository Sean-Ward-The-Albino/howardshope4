package org.howards4hope.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminManagementController {

    @PostMapping("/grant")
    public ResponseEntity<?> grantAdminRole(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                return ResponseEntity.ok().body("Admin role granted to " + email + " (MOCK MODE)");
            }
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            Map<String, Object> claims = new HashMap<>();
            claims.put("admin", true);
            
            FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), claims);
            
            return ResponseEntity.ok().body("Admin role granted to " + email);
        } catch (com.google.firebase.auth.FirebaseAuthException fae) {
            if ("USER_NOT_FOUND".equals(fae.getAuthErrorCode().name()) || fae.getMessage().contains("No user record")) {
                return ResponseEntity.status(404).body("User not found in Firebase. The user must sign up or sign in on the website first before being granted admin rights.");
            }
            return ResponseEntity.status(500).body("Firebase Auth error: " + fae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to grant admin role: " + e.getMessage());
        }
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeAdminRole(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                return ResponseEntity.ok().body("Admin role revoked for " + email + " (MOCK MODE)");
            }
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            Map<String, Object> claims = new HashMap<>();
            claims.put("admin", false);
            
            FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), claims);
            
            return ResponseEntity.ok().body("Admin role revoked for " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to revoke admin role: " + e.getMessage());
        }
    }
}
