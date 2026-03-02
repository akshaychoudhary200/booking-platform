package org.bookingplatform.bookingservice.outbox.infra;

import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("select o from OutboxEvent o where o.publishedAt is null order by o.createdAt asc")
    List<OutboxEvent> findUnpublished();
}