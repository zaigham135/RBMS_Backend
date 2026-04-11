package com.example.backend.repository;

import com.example.backend.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(com.example.backend.enums.Role role);
    long countByStatus(String status);
    long countByRole(com.example.backend.enums.Role role);

    // Get teammates: users who have tasks in the same projects as the given user (excluding the user themselves)
    @Query("""
        SELECT DISTINCT t.assignedTo FROM Task t
        WHERE t.project.id IN (
            SELECT t2.project.id FROM Task t2 WHERE t2.assignedTo.id = :userId
        )
        AND t.assignedTo.id != :userId
        AND t.assignedTo IS NOT NULL
    """)
    List<User> findTeammatesByUserId(@Param("userId") Long userId);
}