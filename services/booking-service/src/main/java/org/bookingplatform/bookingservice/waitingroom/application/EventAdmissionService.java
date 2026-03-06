package org.bookingplatform.bookingservice.waitingroom.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class EventAdmissionService {

    private final StringRedisTemplate redisTemplate;
    private final int maxActiveSlotsPerEvent;
    private final long slotTtlSeconds;
    private final long permitTtlSeconds;

    public EventAdmissionService(
            StringRedisTemplate redisTemplate,
            @Value("${app.waiting-room.maxActiveSlotsPerEvent}") int maxActiveSlotsPerEvent,
            @Value("${app.waiting-room.slotTtlSeconds}") long slotTtlSeconds,
            @Value("${app.waiting-room.permitTtlSeconds}") long permitTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxActiveSlotsPerEvent = maxActiveSlotsPerEvent;
        this.slotTtlSeconds = slotTtlSeconds;
        this.permitTtlSeconds = permitTtlSeconds;
    }

    // ---------------------------
    // Active slot logic (existing)
    // ---------------------------

    public boolean tryAcquireSlot(UUID eventId, UUID bookingId) {
        String activeSlotsKey = activeSlotsKey(eventId);
        String bookingSlotKey = bookingSlotKey(eventId, bookingId);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(bookingSlotKey))) {
            return true;
        }

        Long current = redisTemplate.opsForValue().increment(activeSlotsKey);
        if (current == null) {
            return false;
        }

        if (current > maxActiveSlotsPerEvent) {
            redisTemplate.opsForValue().decrement(activeSlotsKey);
            return false;
        }

        redisTemplate.opsForValue().set(
                bookingSlotKey,
                "1",
                Duration.ofSeconds(slotTtlSeconds));

        redisTemplate.expire(activeSlotsKey, Duration.ofSeconds(slotTtlSeconds));
        return true;
    }

    public void releaseSlot(UUID eventId, UUID bookingId) {
        String activeSlotsKey = activeSlotsKey(eventId);
        String bookingSlotKey = bookingSlotKey(eventId, bookingId);

        Boolean existed = redisTemplate.delete(bookingSlotKey);
        if (Boolean.TRUE.equals(existed)) {
            redisTemplate.opsForValue().decrement(activeSlotsKey);
        }
    }

    public int freeSlots(UUID eventId) {
        String activeSlotsKey = activeSlotsKey(eventId);
        String value = redisTemplate.opsForValue().get(activeSlotsKey);
        int active = value == null ? 0 : Integer.parseInt(value);
        return Math.max(0, maxActiveSlotsPerEvent - active);
    }

    // ---------------------------
    // Queue logic
    // ---------------------------

    public boolean joinQueue(UUID eventId, UUID userId) {
        String queuedMarkerKey = queuedMarkerKey(eventId, userId);

        // already queued
        if (Boolean.TRUE.equals(redisTemplate.hasKey(queuedMarkerKey))) {
            return false;
        }

        redisTemplate.opsForValue().set(
                queuedMarkerKey,
                "1",
                Duration.ofMinutes(10));

        redisTemplate.opsForList().rightPush(queueKey(eventId), userId.toString());
        return true;
    }

    public String popNextQueuedUser(UUID eventId) {
        return redisTemplate.opsForList().leftPop(queueKey(eventId));
    }

    public void removeQueuedMarker(UUID eventId, UUID userId) {
        redisTemplate.delete(queuedMarkerKey(eventId, userId));
    }

    // ---------------------------
    // Permit logic
    // ---------------------------

    public void grantPermit(UUID eventId, UUID userId) {
        redisTemplate.opsForValue().set(
                permitKey(eventId, userId),
                "1",
                Duration.ofSeconds(permitTtlSeconds));
    }

    public boolean consumePermit(UUID eventId, UUID userId) {
        String key = permitKey(eventId, userId);
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public boolean hasPermit(UUID eventId, UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(permitKey(eventId, userId)));
    }

    // ---------------------------
    // Redis keys
    // ---------------------------

    private String activeSlotsKey(UUID eventId) {
        return "event:" + eventId + ":active_slots";
    }

    private String bookingSlotKey(UUID eventId, UUID bookingId) {
        return "event:" + eventId + ":booking:" + bookingId + ":slot";
    }

    private String queueKey(UUID eventId) {
        return "event:" + eventId + ":queue";
    }

    private String queuedMarkerKey(UUID eventId, UUID userId) {
        return "event:" + eventId + ":queued:" + userId;
    }

    private String permitKey(UUID eventId, UUID userId) {
        return "event:" + eventId + ":user:" + userId + ":permit";
    }
}