package org.bookingplatform.bookingservice.waitingroom.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WaitingRoomAdmissionWorker {

    private final EventAdmissionService eventAdmissionService;
    private final int admissionBatchSize;

    public WaitingRoomAdmissionWorker(
            EventAdmissionService eventAdmissionService,
            @Value("${app.waiting-room.admissionBatchSize}") int admissionBatchSize) {
        this.eventAdmissionService = eventAdmissionService;
        this.admissionBatchSize = admissionBatchSize;
    }

    // For now: hardcoded dev event. Later we generalize across many events.
    private static final UUID DEV_EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Scheduled(fixedDelay = 1000)
    public void admitQueuedUsers() {
        int free = eventAdmissionService.freeSlots(DEV_EVENT_ID);
        int admitCount = Math.min(free, admissionBatchSize);

        for (int i = 0; i < admitCount; i++) {
            String nextUser = eventAdmissionService.popNextQueuedUser(DEV_EVENT_ID);
            if (nextUser == null) {
                break;
            }

            UUID userId = UUID.fromString(nextUser);
            eventAdmissionService.removeQueuedMarker(DEV_EVENT_ID, userId);
            eventAdmissionService.grantPermit(DEV_EVENT_ID, userId);
        }
    }
}