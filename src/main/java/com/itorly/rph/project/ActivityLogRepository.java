package com.itorly.rph.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("""
            select log from ActivityLog log
            where log.project.id = :projectId
              and (:actionType is null or log.actionType = :actionType)
              and (:taskId is null or log.task.id = :taskId)
              and (:actorId is null or log.actor.id = :actorId)
            """)
    Page<ActivityLog> findByProjectIdWithFilters(
            @Param("projectId") Long projectId,
            @Param("actionType") ActivityActionType actionType,
            @Param("taskId") Long taskId,
            @Param("actorId") Long actorId,
            Pageable pageable
    );
}
