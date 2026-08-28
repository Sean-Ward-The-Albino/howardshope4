package org.howards4hope.repository;

import org.howards4hope.model.PageAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnalyticsRepository extends JpaRepository<PageAnalytics, Long> {
    
    @Query("SELECT p.path, COUNT(p) as views FROM PageAnalytics p GROUP BY p.path ORDER BY views DESC")
    List<Object[]> getMostVisitedPages();
    
    @Query("SELECT FUNCTION('DATE', p.timestamp) as day, COUNT(p) as views FROM PageAnalytics p GROUP BY FUNCTION('DATE', p.timestamp) ORDER BY day ASC")
    List<Object[]> getViewsPerDay();
}
