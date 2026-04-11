package com.example.backend.repository;

import com.example.backend.Entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByManagerId(Long managerId);
    List<Project> findByManagerIsNotNull();
    Page<Project> findByManagerId(Long managerId, Pageable pageable);
    long countByManagerId(Long managerId);

    // Fetch project with tasks and their assignees eagerly (for member list)
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks t LEFT JOIN FETCH t.assignedTo WHERE p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") Long id);

    // Fetch all projects for a manager with tasks and assignees
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks t LEFT JOIN FETCH t.assignedTo WHERE p.manager.id = :managerId")
    List<Project> findByManagerIdWithMembers(@Param("managerId") Long managerId);

    // Fetch distinct projects where the given employee has at least one assigned task
    @Query("SELECT DISTINCT t.project FROM Task t WHERE t.assignedTo.id = :userId AND t.project IS NOT NULL")
    List<Project> findProjectsByAssignedUserId(@Param("userId") Long userId);

    // Fetch all projects without duplicates (avoids Hibernate collection-join multiplication)
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.manager")
    List<Project> findAllDistinct();
}