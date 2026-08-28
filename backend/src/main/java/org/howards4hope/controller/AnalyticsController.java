package org.howards4hope.controller;

import org.howards4hope.model.PageAnalytics;
import org.howards4hope.repository.AnalyticsRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @PostMapping("/analytics/track")
    public ResponseEntity<?> trackView(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        String visitorId = request.get("visitorId");
        
        if (path == null) {
            return ResponseEntity.badRequest().body("Path is required");
        }
        
        PageAnalytics analytics = new PageAnalytics(path, visitorId != null ? visitorId : "anonymous");
        analyticsRepository.save(analytics);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> response = new HashMap<>();
        
        List<Object[]> mostVisited = analyticsRepository.getMostVisitedPages();
        List<Object[]> viewsPerDay = analyticsRepository.getViewsPerDay();
        long totalViews = analyticsRepository.count();
        
        response.put("totalViews", totalViews);
        response.put("mostVisited", mostVisited);
        response.put("viewsPerDay", viewsPerDay);
        
        return ResponseEntity.ok(response);
    }
}
