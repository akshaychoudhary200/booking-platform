package org.bookingplatform.inventoryservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.inventoryservice.application.InventoryConfirmService;
import org.bookingplatform.inventoryservice.messaging.dto.ConfirmRequestedCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class BookingCommandsConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryConfirmService inventoryConfirmService;

    public BookingCommandsConsumer(ObjectMapper objectMapper, InventoryConfirmService inventoryConfirmService) {
        this.objectMapper = objectMapper;
        this.inventoryConfirmService = inventoryConfirmService;
    }

    @KafkaListener(topics = "booking.commands")
    public void onMessage(String message, Acknowledgment ack) throws Exception {
        ConfirmRequestedCommand cmd = objectMapper.readValue(message, ConfirmRequestedCommand.class);

        if (!"ConfirmRequested".equals(cmd.type())) {
            ack.acknowledge();
            return;
        }

        inventoryConfirmService.confirm(cmd.bookingId(), cmd.holdId(), cmd.userId(), cmd.eventId());

        ack.acknowledge();
    }
}