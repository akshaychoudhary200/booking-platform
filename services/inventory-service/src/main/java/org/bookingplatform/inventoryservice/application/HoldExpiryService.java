package org.bookingplatform.inventoryservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.inventoryservice.domain.Hold;
import org.bookingplatform.inventoryservice.infrastructure.HoldRepository;
import org.bookingplatform.inventoryservice.infrastructure.OutboxRepository;
import org.bookingplatform.inventoryservice.infrastructure.SeatRepository;
import org.bookingplatform.inventoryservice.messaging.dto.HoldExpiredEvent;
import org.bookingplatform.inventoryservice.outbox.OutboxEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HoldExpiryService {

    private final HoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public HoldExpiryService(
            HoldRepository holdRepository,
            SeatRepository seatRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.holdRepository = holdRepository;
        this.seatRepository = seatRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void expireHolds() {
        // small batch; scale later
        var expired = holdRepository.lockExpiredActiveHolds(50);

        for (Hold h : expired) {
            // release seats first (safe either way inside same tx)
            seatRepository.releaseSeatsForHold(h.getHoldId());

            // mark hold expired (idempotent because we only locked ACTIVE holds)
            h.markExpired();

            // emit HoldExpired reliably
            String payload = toJson(new HoldExpiredEvent(
                    "HoldExpired",
                    h.getBookingId(),
                    h.getUserId(),
                    h.getEventId(),
                    h.getHoldId(),
                    h.getExpiresAt()));

            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Hold",
                    h.getHoldId(),
                    "HoldExpired",
                    payload));
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize HoldExpiredEvent", e);
        }
    }
}