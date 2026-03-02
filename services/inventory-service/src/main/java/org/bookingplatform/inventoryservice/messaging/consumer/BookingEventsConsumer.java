package org.bookingplatform.inventoryservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.inventoryservice.application.InventoryHoldService;
import org.bookingplatform.inventoryservice.messaging.dto.HoldRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class BookingEventsConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryHoldService inventoryHoldService;

    public BookingEventsConsumer(ObjectMapper objectMapper, InventoryHoldService inventoryHoldService) {
        this.objectMapper = objectMapper;
        this.inventoryHoldService = inventoryHoldService;
    }

    @KafkaListener(topics = "booking.events")
    public void onMessage(String message, Acknowledgment ack) throws Exception {
        HoldRequestedEvent evt = objectMapper.readValue(message, HoldRequestedEvent.class);

        // Ignore other event types in the same topic (we’ll add more later)
        if (!"HoldRequested".equals(evt.type())) {
            ack.acknowledge();
            return;
        }

        // Do DB work first. If this throws, we DO NOT ack => Kafka redelivers.
        inventoryHoldService.handleHoldRequested(evt.bookingId(), evt.userId(), evt.eventId());

        // Ack only after successful transaction commit path.
        ack.acknowledge();
    }
}