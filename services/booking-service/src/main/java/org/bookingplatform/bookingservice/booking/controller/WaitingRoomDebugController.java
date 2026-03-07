package org.bookingplatform.bookingservice.booking.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class WaitingRoomDebugController {

    private final StringRedisTemplate redisTemplate;

    public WaitingRoomDebugController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/debug/waiting-room/{eventId}")
    public Map<String, String> activeSlots(@PathVariable UUID eventId) {
        String key = "event:" + eventId + ":active_slots";
        String value = redisTemplate.opsForValue().get(key);
        return Map.of(
                "eventId", eventId.toString(),
                "activeSlots", value == null ? "0" : value);
    }
}