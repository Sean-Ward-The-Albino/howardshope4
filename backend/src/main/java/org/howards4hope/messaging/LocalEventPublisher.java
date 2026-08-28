package org.howards4hope.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Local / In-Memory Event Publisher implementation.
 * Provides high-speed asynchronous event processing without requiring external cloud infrastructure.
 */
@Service
public class LocalEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LocalEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeadLetterQueueService dlqService;

    public LocalEventPublisher(ApplicationEventPublisher applicationEventPublisher, DeadLetterQueueService dlqService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.dlqService = dlqService;
    }

    @Override
    public <T> void publish(String topic, DomainEvent<T> event) {
        try {
            logger.info("📢 [LOCAL EVENTBUS] Publishing event '{}' (ID: {}) to channel '{}'",
                    event.getEventType(), event.getEventId(), topic);
            
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            logger.error("Failed to publish local event {}: {}", event.getEventId(), e.getMessage());
            dlqService.handleFailure(topic, event, e);
        }
    }
}
