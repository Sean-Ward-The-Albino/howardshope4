package org.howards4hope.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dead Letter Queue (DLQ) Service.
 * Captures failed event processing attempts after max retries (e.g. 3 attempts),
 * enabling automated logging, audit trails, and administrative re-drive.
 */
@Service
public class DeadLetterQueueService {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueService.class);
    private static final int MAX_RETRY_LIMIT = 3;

    private final Map<String, DeadLetterMessage> deadLetterStore = new ConcurrentHashMap<>();
    private final AtomicLong dlqCounter = new AtomicLong(0);

    /**
     * Handle message processing failure. Routes to DLQ if max retries exceeded.
     */
    public <T> boolean handleFailure(String topic, DomainEvent<T> event, Throwable error) {
        event.setRetryCount(event.getRetryCount() + 1);

        if (event.getRetryCount() >= MAX_RETRY_LIMIT) {
            routeToDlq(topic, event, error);
            return false; // Terminal failure -> sent to DLQ
        }

        logger.warn("Message retry [{}/{}] on topic [{}], eventId [{}]: {}",
                event.getRetryCount(), MAX_RETRY_LIMIT, topic, event.getEventId(), error.getMessage());
        return true; // Eligible for immediate retry
    }

    /**
     * Route message permanently to the Dead Letter Queue.
     */
    public <T> void routeToDlq(String topic, DomainEvent<T> event, Throwable error) {
        String dlqId = "dlq-" + dlqCounter.incrementAndGet();
        DeadLetterMessage dlqMessage = new DeadLetterMessage(
                dlqId,
                topic,
                event.getEventId(),
                event.getEventType(),
                event.getPayload(),
                event.getRetryCount(),
                error.getMessage(),
                System.currentTimeMillis()
        );

        deadLetterStore.put(dlqId, dlqMessage);

        logger.error("🚨 [DLQ ROUTED] Message {} sent to Dead Letter Queue for topic '{}'. Error: {}",
                event.getEventId(), topic, error.getMessage());
    }

    public Map<String, DeadLetterMessage> getDeadLetterMessages() {
        return deadLetterStore;
    }

    public DeadLetterMessage getMessage(String dlqId) {
        return deadLetterStore.get(dlqId);
    }

    public void removeMessage(String dlqId) {
        deadLetterStore.remove(dlqId);
    }

    /**
     * Record representing a dead-lettered message.
     */
    public record DeadLetterMessage(
            String dlqId,
            String originalTopic,
            String eventId,
            String eventType,
            Object payload,
            int retryAttempts,
            String failureReason,
            long failureTimestamp
    ) {}
}
