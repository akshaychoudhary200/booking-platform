package org.bookingplatform.bookingservice.waitingroom.application;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class WaitingRoomController {

    private final EventAdmissionService eventAdmissionService;

    public WaitingRoomController(EventAdmissionService eventAdmissionService) {
        this.eventAdmissionService = eventAdmissionService;
    }

    @PostMapping("/{eventId}/queue/join")
    public ResponseEntity<?> joinQueue(Authentication authentication, @PathVariable UUID eventId) {
        UUID userId = (UUID) authentication.getPrincipal();
        boolean joined = eventAdmissionService.joinQueue(eventId, userId);

        return ResponseEntity.ok(Map.of(
                "eventId", eventId.toString(),
                "userId", userId.toString(),
                "joined", joined));
    }

    @GetMapping("/{eventId}/queue/status")
    public ResponseEntity<?> queueStatus(Authentication authentication, @PathVariable UUID eventId) {
        UUID userId = (UUID) authentication.getPrincipal();
        boolean hasPermit = eventAdmissionService.hasPermit(eventId, userId);

        return ResponseEntity.ok(Map.of(
                "eventId", eventId.toString(),
                "userId", userId.toString(),
                "hasPermit", hasPermit));
    }
}