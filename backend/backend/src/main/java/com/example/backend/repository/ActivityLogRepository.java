package com.example.backend.repository;

import com.example.backend.Entities.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a LEFT JOIN FETCH a.user ORDER BY a.createdAt DESC")
    java.util.List<ActivityLog> findRecentWithUser(Pageable pageable);

    @Query("SELECT a FROM ActivityLog a LEFT JOIN FETCH a.user WHERE a.projectId IN :projectIds ORDER BY a.createdAt DESC")
    java.util.List<ActivityLog> findRecentByProjectIds(@Param("projectIds") java.util.List<Long> projectIds, Pageable pageable);
}
