package org.howards4hope.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdminManagementController {

    @PostMapping("/admin/roles/grant")
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
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to grant admin role: " + e.getMessage());
        }
    }
}
