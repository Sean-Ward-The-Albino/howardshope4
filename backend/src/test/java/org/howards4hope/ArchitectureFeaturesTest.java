package org.howards4hope;

import org.howards4hope.config.CacheConfig;
import org.howards4hope.messaging.DeadLetterQueueService;
import org.howards4hope.messaging.DomainEvent;
import org.howards4hope.model.AccountProfile;
import org.howards4hope.model.Event;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.service.AccountCacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
public class ArchitectureFeaturesTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DeadLetterQueueService dlqService;

    @Autowired
    private AccountCacheService accountCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Verify Keyset (Cursor-Based) Pagination with Unique Identifiers")
    void testKeysetPagination() {
        // Seed sample events if empty
        if (eventRepository.count() == 0) {
            eventRepository.save(new Event("Event 1", "Desc 1", "2026-09-01", "10:00 AM", "Long Beach", 0, "banner1", "Youth", "#2563EB"));
            eventRepository.save(new Event("Event 2", "Desc 2", "2026-09-15", "11:00 AM", "Long Beach", 10, "banner2", "Caregivers", "#F39C12"));
            eventRepository.save(new Event("Event 3", "Desc 3", "2026-10-01", "12:00 PM", "Long Beach", 0, "banner3", "Parents", "#007C92"));
        }

        // 1. First page (cursorDate = null, cursorId = null, limit = 2)
        List<Event> firstPage = eventRepository.findKeysetPage(null, null, PageRequest.of(0, 2));
        Assertions.assertFalse(firstPage.isEmpty(), "First keyset page should return items");
        
        Event lastItem = firstPage.get(firstPage.size() - 1);
        String nextCursorDate = lastItem.getDate();
        Long nextCursorId = lastItem.getId();

        // 2. Next keyset page using previous item cursor
        List<Event> secondPage = eventRepository.findKeysetPage(nextCursorDate, nextCursorId, PageRequest.of(0, 2));
        Assertions.assertNotNull(secondPage, "Second keyset page should execute successfully");

        // Verify keyset ordering: all second page dates/ids must be <= cursor
        for (Event e : secondPage) {
            Assertions.assertTrue(
                e.getDate().compareTo(nextCursorDate) < 0 || 
                (e.getDate().equals(nextCursorDate) && e.getId() < nextCursorId),
                "Keyset pagination must return strictly older/lesser composite key records"
            );
        }
    }

    @Test
    @DisplayName("Verify Dead Letter Queue (DLQ) Routing after Max Retries")
    void testDeadLetterQueueRouting() {
        DomainEvent<String> sampleEvent = new DomainEvent<>("TICKET_PURCHASE_SYNC", "payload-ticket-999");
        String topic = "h4h-tickets";
        RuntimeException simulatedError = new RuntimeException("Simulated External Payment Gateway Timeout");

        // Attempt 1 -> retry eligible
        boolean retry1 = dlqService.handleFailure(topic, sampleEvent, simulatedError);
        Assertions.assertTrue(retry1, "Attempt 1 should be eligible for retry");

        // Attempt 2 -> retry eligible
        boolean retry2 = dlqService.handleFailure(topic, sampleEvent, simulatedError);
        Assertions.assertTrue(retry2, "Attempt 2 should be eligible for retry");

        // Attempt 3 -> max retries exceeded, sent to DLQ
        boolean retry3 = dlqService.handleFailure(topic, sampleEvent, simulatedError);
        Assertions.assertFalse(retry3, "Attempt 3 should exceed limit and route to DLQ");

        // Verify message exists in DLQ store
        Assertions.assertFalse(dlqService.getDeadLetterMessages().isEmpty(), "DLQ store should contain the dead-lettered message");
    }

    @Test
    @DisplayName("Verify L1 Caffeine Account Caching and Eviction")
    void testL1CaffeineAccountCaching() {
        String testEmail = "caregiver.mentor@howards4hope.org";

        // Initial fetch -> populates L1 cache
        AccountProfile profile1 = accountCacheService.getAccountProfile(testEmail);
        Assertions.assertNotNull(profile1);
        Assertions.assertEquals(testEmail, profile1.getEmail());

        // Check L1 cache direct access
        Cache cache = cacheManager.getCache(CacheConfig.ACCOUNTS_CACHE);
        Assertions.assertNotNull(cache, "Accounts cache should exist");
        AccountProfile cachedVal = cache.get(testEmail, AccountProfile.class);
        Assertions.assertNotNull(cachedVal, "Account should be cached in L1 Caffeine cache");

        // Evict
        accountCacheService.evictAccount(testEmail);
        AccountProfile afterEvict = cache.get(testEmail, AccountProfile.class);
        Assertions.assertNull(afterEvict, "Account should be removed from cache after eviction");
    }
}
