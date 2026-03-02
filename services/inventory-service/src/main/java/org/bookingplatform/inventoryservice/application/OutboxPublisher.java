package org.bookingplatform.inventoryservice.application;

import org.bookingplatform.inventoryservice.infrastructure.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishUnsent() {
        var events = outboxRepository.findUnpublished();
        for (var e : events) {
            kafkaTemplate.send("inventory.events", e.getAggregateId().toString(), e.getPayloadJson());
            e.markPublished();
        }
    }
}