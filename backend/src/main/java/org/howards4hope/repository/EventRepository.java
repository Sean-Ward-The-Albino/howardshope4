package org.howards4hope.repository;

import org.howards4hope.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategoryIgnoreCase(String category);

    /**
     * Keyset (cursor-based) pagination query with B-Tree index support.
     * Selects records strictly older/lesser than the composite cursor (cursorDate, cursorId).
     */
    @Query("SELECT e FROM Event e WHERE (:cursorDate IS NULL OR e.date < :cursorDate OR (e.date = :cursorDate AND e.id < :cursorId)) ORDER BY e.date DESC, e.id DESC")
    List<Event> findKeysetPage(@Param("cursorDate") String cursorDate, @Param("cursorId") Long cursorId, Pageable pageable);

    /**
     * Full-Text search across title, description, and category.
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.category) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY e.date DESC")
    List<Event> searchEvents(@Param("query") String query);
}

