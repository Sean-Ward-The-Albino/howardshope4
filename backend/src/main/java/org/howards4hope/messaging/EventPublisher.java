package org.howards4hope.messaging;

/**
 * Pluggable Event Publisher interface.
 * Abstracts messaging so the system can seamlessly switch between GCP Pub/Sub,
 * RabbitMQ, Kafka, or in-memory EventBus at any moment.
 */
public interface EventPublisher {
    
    /**
     * Publish an event to a topic or channel.
     *
     * @param topic Destination topic (e.g. "h4h-tickets", "h4h-donations", "h4h-cache-sync")
     * @param event The domain event payload
     * @param <T>   Payload type
     */
    <T> void publish(String topic, DomainEvent<T> event);

    /**
     * Convenience method to publish typed payload to a topic.
     */
    default <T> void publish(String topic, String eventType, T payload) {
        publish(topic, new DomainEvent<>(eventType, payload));
    }
}
