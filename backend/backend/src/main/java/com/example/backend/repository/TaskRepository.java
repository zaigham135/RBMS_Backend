package com.example.backend.repository;

import com.example.backend.Entities.Task;
import com.example.backend.enums.Priority;
import com.example.backend.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Employee → only their tasks
    Page<Task> findByAssignedToId(Long userId, Pageable pageable);

    // Employee → their tasks filtered by project
    Page<Task> findByAssignedToIdAndProjectId(Long userId, Long projectId, Pageable pageable);
    Page<Task> findByAssignedToIdAndProjectIdAndStatus(Long userId, Long projectId, Status status, Pageable pageable);

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
    long countByStatus(Status status);
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

    // Count tasks for manager's projects by status
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id IN (SELECT p.id FROM Project p WHERE p.manager.id = :managerId) AND t.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") Status status);

    // Count tasks for manager's projects by status and date range (for "this week")
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id IN (SELECT p.id FROM Project p WHERE p.manager.id = :managerId) AND t.status = :status AND t.createdAt >= :from AND t.createdAt <= :to")
    long countByManagerIdAndStatusAndDateRange(@Param("managerId") Long managerId, @Param("status") Status status, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    // Count IN_PROGRESS tasks for a specific user (for workload)
    long countByAssignedToIdAndStatus(Long userId, Status status);

    // Get distinct assignees (team members) for a specific project
    @Query("SELECT DISTINCT t.assignedTo FROM Task t WHERE t.project.id = :projectId AND t.assignedTo IS NOT NULL")
    java.util.List<com.example.backend.Entities.User> findDistinctAssigneesByProjectId(@Param("projectId") Long projectId);

    // Tasks for manager with priority filter
    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id IN (SELECT p.id FROM Project p WHERE p.manager.id = :managerId)
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:assignedTo IS NULL OR t.assignedTo.id = :assignedTo)
        AND (:projectId IS NULL OR t.project.id = :projectId)
    """)
    Page<Task> findManagerTasksFiltered(
        @Param("managerId") Long managerId,
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("projectId") Long projectId,
        @Param("assignedTo") Long assignedTo,
        Pageable pageable
    );

    // Completion trend: tasks done per day for manager's projects
    @Query("""
        SELECT CAST(t.createdAt AS date), COUNT(t)
        FROM Task t
        WHERE t.project.id IN (SELECT p.id FROM Project p WHERE p.manager.id = :managerId)
        AND t.status = 'DONE'
        AND t.createdAt >= :from
        GROUP BY CAST(t.createdAt AS date)
        ORDER BY CAST(t.createdAt AS date)
    """)
    java.util.List<Object[]> findCompletionTrendForManager(@Param("managerId") Long managerId, @Param("from") java.time.LocalDateTime from);

    // Count tasks completed this month for a user
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'DONE' AND t.createdAt >= :from AND t.createdAt <= :to")
    long countCompletedThisMonthForUser(@Param("userId") Long userId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}