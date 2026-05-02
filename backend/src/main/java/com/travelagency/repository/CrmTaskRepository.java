package com.travelagency.repository;

import com.travelagency.domain.CrmTask;
import com.travelagency.domain.CrmTaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmTaskRepository extends JpaRepository<CrmTask, UUID> {

    @EntityGraph(attributePaths = {"assignee", "booking", "booking.tour"})
    List<CrmTask> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"assignee", "booking", "booking.tour"})
    List<CrmTask> findByAssigneeIdOrderByCreatedAtDesc(UUID assigneeId);

    @EntityGraph(attributePaths = {"assignee", "booking", "booking.tour"})
    List<CrmTask> findByStatusInAndDueDateLessThanEqualOrderByDueDateAsc(
            List<CrmTaskStatus> statuses, LocalDate dueDate);
}
