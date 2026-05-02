package com.travelagency.repository;

import com.travelagency.domain.SupportMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {}
