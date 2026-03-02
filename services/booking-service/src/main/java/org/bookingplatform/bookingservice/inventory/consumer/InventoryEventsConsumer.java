package org.bookingplatform.bookingservice.inventory.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.bookingservice.booking.dto.HoldCreatedEvent;
import org.bookingplatform.bookingservice.booking.dto.HoldRejectedEvent;
import org.bookingplatform.bookingservice.booking.service.BookingHoldResultService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventsConsumer {

    private final ObjectMapper objectMapper;
    private final BookingHoldResultService bookingHoldResultService;

    public InventoryEventsConsumer(ObjectMapper objectMapper, BookingHoldResultService bookingHoldResultService) {
        this.objectMapper = objectMapper;
        this.bookingHoldResultService = bookingHoldResultService;
    }

    @KafkaListener(topics = "inventory.events")
    public void onMessage(String message, Acknowledgment ack) throws Exception {

        JsonNode root = objectMapper.readTree(message);
        String type = root.path("type").asText();

        if ("HoldCreated".equals(type)) {
            HoldCreatedEvent evt = objectMapper.treeToValue(root, HoldCreatedEvent.class);
            bookingHoldResultService.applyHoldCreated(evt.bookingId(), evt.holdId(), evt.expiresAt(), evt.seatsHeld());
            ack.acknowledge();
            return;
        }

        if ("HoldRejected".equals(type)) {
            HoldRejectedEvent evt = objectMapper.treeToValue(root, HoldRejectedEvent.class);
            bookingHoldResultService.applyHoldRejected(evt.bookingId());
            ack.acknowledge();
            return;
        }

        // Unknown event type -> ignore safely
        ack.acknowledge();
    }
}