package org.howards4hope.messaging;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Standardized Domain Event payload for GCP Pub/Sub and asynchronous event processing.
 */
public class DomainEvent<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private int retryCount;
    private T payload;

    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.retryCount = 0;
    }

    public DomainEvent(String eventType, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.retryCount = 0;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
