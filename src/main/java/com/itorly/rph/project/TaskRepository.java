package com.itorly.rph.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByColumnId(Long columnId, Pageable pageable);

    List<Task> findByColumnIdOrderByPositionAscIdAsc(Long columnId);

    @Query("select max(t.position) from Task t where t.column.id = :columnId")
    Integer findMaxPositionByColumnId(@Param("columnId") Long columnId);
}
