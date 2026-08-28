package org.howards4hope.controller;

import org.howards4hope.messaging.DeadLetterQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin REST controller for inspecting and managing the Dead Letter Queue (DLQ).
 */
@RestController
@RequestMapping("/api/admin/dlq")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeadLetterQueueController {

    private final DeadLetterQueueService dlqService;

    public DeadLetterQueueController(DeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping("/messages")
    public ResponseEntity<Map<String, DeadLetterQueueService.DeadLetterMessage>> listDlqMessages() {
        return ResponseEntity.ok(dlqService.getDeadLetterMessages());
    }

    @GetMapping("/messages/{dlqId}")
    public ResponseEntity<DeadLetterQueueService.DeadLetterMessage> getDlqMessage(@PathVariable String dlqId) {
        DeadLetterQueueService.DeadLetterMessage msg = dlqService.getMessage(dlqId);
        if (msg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(msg);
    }

    @DeleteMapping("/messages/{dlqId}")
    public ResponseEntity<Void> dismissDlqMessage(@PathVariable String dlqId) {
        dlqService.removeMessage(dlqId);
        return ResponseEntity.ok().build();
    }
}
