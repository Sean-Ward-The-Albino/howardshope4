package org.howards4hope.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final Map<String, Object> settingsStore = new ConcurrentHashMap<>();

    @GetMapping("/{key}")
    public ResponseEntity<?> getSetting(@PathVariable String key) {
        Object val = settingsStore.get(key);
        if (val == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(val);
    }

    @PostMapping("/{key}")
    public ResponseEntity<?> saveSetting(@PathVariable String key, @RequestBody Map<String, Object> payload) {
        settingsStore.put(key, payload);
        return ResponseEntity.ok(Map.of("success", true, "key", key, "message", "Setting saved successfully"));
    }
}
