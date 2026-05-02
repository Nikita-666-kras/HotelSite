package com.travelagency.repository;

import com.travelagency.domain.SupportConversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, UUID> {

    @Query(
            """
            SELECT DISTINCT c FROM SupportConversation c
            LEFT JOIN FETCH c.messages msg
            LEFT JOIN FETCH msg.author
            WHERE c.id = :id
            """)
    Optional<SupportConversation> findDetailedById(@Param("id") UUID id);

    List<SupportConversation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SupportConversation> findAllByOrderByCreatedAtDesc();
}
