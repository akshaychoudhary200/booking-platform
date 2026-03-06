package org.bookingplatform.inventoryservice.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.inventoryservice.application.InventoryCancelService;
import org.bookingplatform.inventoryservice.application.InventoryConfirmService;
import org.bookingplatform.inventoryservice.messaging.dto.CancelRequestedCommand;
import org.bookingplatform.inventoryservice.messaging.dto.ConfirmRequestedCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class BookingCommandsConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryConfirmService inventoryConfirmService;
    private final InventoryCancelService inventoryCancelService;

    public BookingCommandsConsumer(
            ObjectMapper objectMapper,
            InventoryConfirmService inventoryConfirmService,
            InventoryCancelService inventoryCancelService) {
        this.objectMapper = objectMapper;
        this.inventoryConfirmService = inventoryConfirmService;
        this.inventoryCancelService = inventoryCancelService;
    }

    @KafkaListener(topics = "booking.commands")
    public void onMessage(String message, Acknowledgment ack) throws Exception {
        JsonNode root = objectMapper.readTree(message);
        String type = root.path("type").asText();

        if ("ConfirmRequested".equals(type)) {
            ConfirmRequestedCommand cmd = objectMapper.treeToValue(root, ConfirmRequestedCommand.class);
            inventoryConfirmService.confirm(cmd.bookingId(), cmd.holdId(), cmd.userId(), cmd.eventId());
            ack.acknowledge();
            return;
        }

        if ("CancelRequested".equals(type)) {
            CancelRequestedCommand cmd = objectMapper.treeToValue(root, CancelRequestedCommand.class);
            inventoryCancelService.cancel(cmd.bookingId(), cmd.holdId(), cmd.userId(), cmd.eventId());
            ack.acknowledge();
            return;
        }

        ack.acknowledge();
    }
}