package org.howards4hope.controller;

import org.howards4hope.config.CacheConfig;
import org.howards4hope.dto.KeysetPageResponse;
import org.howards4hope.messaging.EventPublisher;
import org.howards4hope.model.Event;
import org.howards4hope.repository.EventRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EventController {

    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;

    public EventController(EventRepository eventRepository, EventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/events")
    @Cacheable(value = CacheConfig.EVENTS_CACHE, key = "'all_events'")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Keyset (Cursor-based) Pagination Endpoint.
     * Fast O(1) performance powered by composite (date, id) B-Tree indexing.
     */
    @GetMapping("/events/keyset")
    public ResponseEntity<KeysetPageResponse<Event>> getEventsKeyset(
            @RequestParam(required = false) String cursorDate,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int limit) {
        
        int pageSize = Math.min(Math.max(limit, 1), 50);
        // Fetch pageSize + 1 to determine hasNext accurately
        List<Event> records = eventRepository.findKeysetPage(cursorDate, cursorId, PageRequest.of(0, pageSize + 1));

        boolean hasNext = records.size() > pageSize;
        List<Event> items = hasNext ? records.subList(0, pageSize) : records;

        String nextCursorDate = null;
        Long nextCursorId = null;
        if (!items.isEmpty()) {
            Event last = items.get(items.size() - 1);
            nextCursorDate = last.getDate();
            nextCursorId = last.getId();
        }

        return ResponseEntity.ok(new KeysetPageResponse<>(items, nextCursorDate, nextCursorId, hasNext, pageSize));
    }

    /**
     * Full-Text Search Endpoint.
     */
    @GetMapping("/events/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(eventRepository.findAll());
        }
        return ResponseEntity.ok(eventRepository.searchEvents(query.trim()));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- ADMIN SECURED ENDPOINTS ---

    @PostMapping("/admin/events")
    @CacheEvict(value = CacheConfig.EVENTS_CACHE, allEntries = true)
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        if (event.getTitle() == null || event.getDate() == null) {
            return ResponseEntity.badRequest().build();
        }
        Event savedEvent = eventRepository.save(event);

        // Publish event to messaging bus
        eventPublisher.publish("h4h-events", "EVENT_CREATED", savedEvent);

        return ResponseEntity.ok(savedEvent);
    }

    @PutMapping("/admin/events/{id}")
    @CacheEvict(value = CacheConfig.EVENTS_CACHE, allEntries = true)
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setDate(eventDetails.getDate());
        event.setTime(eventDetails.getTime());
        event.setLocation(eventDetails.getLocation());
        event.setPrice(eventDetails.getPrice());
        event.setBannerUrl(eventDetails.getBannerUrl());
        event.setCategory(eventDetails.getCategory());
        event.setColor(eventDetails.getColor());
        event.setAllowInstallments(eventDetails.isAllowInstallments());
        event.setInstallmentCycles(eventDetails.getInstallmentCycles());
        event.setInstallmentFrequency(eventDetails.getInstallmentFrequency());

        Event updatedEvent = eventRepository.save(event);
        eventPublisher.publish("h4h-events", "EVENT_UPDATED", updatedEvent);

        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/admin/events/{id}")
    @CacheEvict(value = CacheConfig.EVENTS_CACHE, allEntries = true)
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        eventRepository.deleteById(id);
        eventPublisher.publish("h4h-events", "EVENT_DELETED", id);
        return ResponseEntity.ok().build();
    }
}
