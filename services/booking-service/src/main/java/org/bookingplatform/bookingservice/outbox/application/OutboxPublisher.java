package org.bookingplatform.bookingservice.outbox.application;

import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    private String topicFor(String eventType) {
        return switch (eventType) {
            case "HoldRequested" -> "booking.events";
            case "ConfirmRequested" -> "booking.commands";
            case "CancelRequested" -> "booking.commands";
            case "PaymentRequested" -> "booking.commands";
            default -> "booking.events";
        };
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishUnsent() {
        var events = outboxRepository.findUnpublished();
        for (var e : events) {
            kafkaTemplate.send(topicFor(e.getEventType()), e.getAggregateId().toString(), e.getPayloadJson());
            e.markPublished();
        }
    }

}