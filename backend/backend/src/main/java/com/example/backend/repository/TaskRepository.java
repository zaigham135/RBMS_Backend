package com.example.backend.repository;

import com.example.backend.Entities.Task;
import com.example.backend.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Employee → only their tasks
    Page<Task> findByAssignedToId(Long userId, Pageable pageable);

    // Filter by project
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    // Filter by status
    Page<Task> findByStatus(Status status, Pageable pageable);

    // Combined filter
    Page<Task> findByProjectIdAndStatus(Long projectId, Status status, Pageable pageable);

    // Manager → tasks in their projects + tasks assigned to them
    @Query("""
    SELECT t FROM Task t
    WHERE t.project.id IN (
        SELECT p.id FROM Project p WHERE p.manager.id = :managerId
    )
    OR t.assignedTo.id = :managerId
""")
    Page<Task> findTasksForManager(@Param("managerId") Long managerId, Pageable pageable);
    Page<Task> findByAssignedToIdAndStatus(Long userId, Status status, Pageable pageable);
    @Query("""
    SELECT t FROM Task t
    WHERE (t.project.id IN (
        SELECT p.id FROM Project p WHERE p.manager.id = :managerId
    )
    OR t.assignedTo.id = :managerId)
    AND t.status = :status
""")
    Page<Task> findTasksForManagerWithStatus(
            @Param("managerId") Long managerId,
            @Param("status") Status status,
            Pageable pageable
    );
    @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.assignedTo
    LEFT JOIN FETCH t.project
    LEFT JOIN FETCH t.createdBy
    WHERE t.assignedTo.id = :userId
""")
    Page<Task> findTasksWithRelations(@Param("userId") Long userId, Pageable pageable);
    @Query("""
    SELECT DISTINCT t FROM Task t
    LEFT JOIN FETCH t.assignedTo
    LEFT JOIN FETCH t.project
    LEFT JOIN FETCH t.createdBy
    WHERE t.assignedTo.id = :userId AND t.status = :status
""")
    Page<Task> findTasksWithRelationsAndStatus(
            @Param("userId") Long userId,
            @Param("status") Status status,
            Pageable pageable
    );
}