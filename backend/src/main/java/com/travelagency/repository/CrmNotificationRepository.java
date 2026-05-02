package com.travelagency.repository;

import com.travelagency.domain.CrmNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmNotificationRepository extends JpaRepository<CrmNotification, UUID> {

    @EntityGraph(attributePaths = {"recipient"})
    List<CrmNotification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    @EntityGraph(attributePaths = {"recipient"})
    List<CrmNotification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId);
}
