package org.howards4hope.controller;

import org.howards4hope.model.Donation;
import org.howards4hope.model.PageAnalytics;
import org.howards4hope.model.Ticket;
import org.howards4hope.repository.AnalyticsRepository;
import org.howards4hope.repository.DonationRepository;
import org.howards4hope.repository.TicketRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;
    private final TicketRepository ticketRepository;
    private final DonationRepository donationRepository;

    public AnalyticsController(AnalyticsRepository analyticsRepository,
                               TicketRepository ticketRepository,
                               DonationRepository donationRepository) {
        this.analyticsRepository = analyticsRepository;
        this.ticketRepository = ticketRepository;
        this.donationRepository = donationRepository;
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
    public ResponseEntity<Map<String, Object>> getAnalytics(@RequestParam(defaultValue = "30d") String timeframe) {
        Map<String, Object> response = new HashMap<>();
        
        long totalViews = analyticsRepository.count();
        long totalUniqueVisitors = analyticsRepository.countUniqueVisitors();
        
        // Active in last 15 minutes
        java.time.LocalDateTime fifteenMinAgo = java.time.LocalDateTime.now().minusMinutes(15);
        long activeNow = analyticsRepository.countUniqueVisitorsSince(fifteenMinAgo);

        List<Object[]> topPages = analyticsRepository.getTopPagesWithUniques();
        List<Object[]> dailyStats = analyticsRepository.getDailyViewsAndUniques();
        
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Donation> allDonations = donationRepository.findAll();

        double ticketRevenue = allTickets.stream().mapToDouble(Ticket::getPricePaid).sum();
        double donationRevenue = allDonations.stream().mapToDouble(Donation::getAmount).sum();
        double totalRevenue = ticketRevenue + donationRevenue;

        int days = "all".equalsIgnoreCase(timeframe) ? 365 :
                   ("180d".equalsIgnoreCase(timeframe) ? 180 : 
                   ("90d".equalsIgnoreCase(timeframe) ? 90 : 
                   ("7d".equalsIgnoreCase(timeframe) ? 7 : 30)));

        Map<String, Long> viewsByDate = new HashMap<>();
        Map<String, Long> uniquesByDate = new HashMap<>();
        for (Object[] row : dailyStats) {
            if (row != null && row.length >= 3 && row[0] != null) {
                String d = row[0].toString();
                viewsByDate.put(d, ((Number) row[1]).longValue());
                uniquesByDate.put(d, ((Number) row[2]).longValue());
            }
        }

        Map<String, Long> ticketsByDate = new HashMap<>();
        Map<String, Double> revenueByDate = new HashMap<>();

        for (Ticket t : allTickets) {
            String pDate = t.getPurchaseDate();
            if (pDate != null) {
                ticketsByDate.put(pDate, ticketsByDate.getOrDefault(pDate, 0L) + t.getQuantity());
                revenueByDate.put(pDate, revenueByDate.getOrDefault(pDate, 0.0) + t.getPricePaid());
            }
        }

        for (Donation d : allDonations) {
            String dDate = d.getDonationDate();
            if (dDate != null) {
                revenueByDate.put(dDate, revenueByDate.getOrDefault(dDate, 0.0) + d.getAmount());
            }
        }

        List<Map<String, Object>> dailyReport = new ArrayList<>();
        List<Object[]> viewsPerDayList = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String dStr = d.toString();
            long dayViews = viewsByDate.getOrDefault(dStr, 0L);
            long dayUniques = uniquesByDate.getOrDefault(dStr, 0L);
            long dayTickets = ticketsByDate.getOrDefault(dStr, 0L);
            double dayRev = revenueByDate.getOrDefault(dStr, 0.0);

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", dStr);
            dayMap.put("views", dayViews);
            dayMap.put("unique", dayUniques);
            dayMap.put("tickets", dayTickets);
            dayMap.put("revenue", dayRev);
            dayMap.put("conversion", dayViews > 0 ? String.format("%.1f", ((double) dayTickets / dayViews) * 100) : "0.0");
            dayMap.put("source", dayViews > 0 ? "Direct / Organic" : "—");
            dailyReport.add(dayMap);

            viewsPerDayList.add(new Object[]{ dStr, dayViews, dayUniques });
        }

        List<Map<String, Object>> formattedTopPages = new ArrayList<>();
        for (Object[] row : topPages) {
            if (row != null && row.length >= 3 && row[0] != null) {
                Map<String, Object> page = new HashMap<>();
                page.put("path", row[0].toString());
                page.put("views", ((Number) row[1]).longValue());
                page.put("uniques", ((Number) row[2]).longValue());
                formattedTopPages.add(page);
            }
        }

        response.put("totalViews", totalViews);
        response.put("uniqueVisitors", totalUniqueVisitors);
        response.put("activeNow", Math.max(1, activeNow));
        response.put("totalTickets", allTickets.size());
        response.put("totalDonations", allDonations.size());
        response.put("totalRevenue", totalRevenue);
        response.put("mostVisited", formattedTopPages);
        response.put("viewsPerDay", viewsPerDayList);
        response.put("dailyReport", dailyReport);
        response.put("timeframe", timeframe);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/admin/analytics/export", produces = "text/csv")
    public ResponseEntity<String> exportAnalyticsCsv(@RequestParam(defaultValue = "30d") String timeframe) {
        int days = "all".equalsIgnoreCase(timeframe) ? 365 :
                   ("180d".equalsIgnoreCase(timeframe) ? 180 : 
                   ("90d".equalsIgnoreCase(timeframe) ? 90 : 
                   ("7d".equalsIgnoreCase(timeframe) ? 7 : 30)));

        List<Object[]> dailyStats = analyticsRepository.getDailyViewsAndUniques();
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Donation> allDonations = donationRepository.findAll();

        Map<String, Long> viewsByDate = new HashMap<>();
        Map<String, Long> uniquesByDate = new HashMap<>();
        for (Object[] row : dailyStats) {
            if (row != null && row.length >= 3 && row[0] != null) {
                String d = row[0].toString();
                viewsByDate.put(d, ((Number) row[1]).longValue());
                uniquesByDate.put(d, ((Number) row[2]).longValue());
            }
        }

        Map<String, Long> ticketsByDate = new HashMap<>();
        Map<String, Double> revenueByDate = new HashMap<>();

        for (Ticket t : allTickets) {
            String pDate = t.getPurchaseDate();
            if (pDate != null) {
                ticketsByDate.put(pDate, ticketsByDate.getOrDefault(pDate, 0L) + t.getQuantity());
                revenueByDate.put(pDate, revenueByDate.getOrDefault(pDate, 0.0) + t.getPricePaid());
            }
        }

        for (Donation d : allDonations) {
            String dDate = d.getDonationDate();
            if (dDate != null) {
                revenueByDate.put(dDate, revenueByDate.getOrDefault(dDate, 0.0) + d.getAmount());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Total Page Views,Unique Visitors,Passes Reserved,Revenue ($),Conversion Rate (%)\n");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String dStr = d.toString();
            long dayViews = viewsByDate.getOrDefault(dStr, 0L);
            long dayUniques = uniquesByDate.getOrDefault(dStr, 0L);
            long dayTickets = ticketsByDate.getOrDefault(dStr, 0L);
            double dayRev = revenueByDate.getOrDefault(dStr, 0.0);
            String conv = dayViews > 0 ? String.format(Locale.US, "%.1f", ((double) dayTickets / dayViews) * 100) : "0.0";

            sb.append(dStr).append(",")
              .append(dayViews).append(",")
              .append(dayUniques).append(",")
              .append(dayTickets).append(",")
              .append(String.format(Locale.US, "%.2f", dayRev)).append(",")
              .append(conv).append("%\n");
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"howards4hope_traffic_report_" + timeframe + ".csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(sb.toString());
    }
}
