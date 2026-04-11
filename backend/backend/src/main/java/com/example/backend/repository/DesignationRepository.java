package com.example.backend.repository;

import com.example.backend.Entities.Designation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByNameIgnoreCase(String name);
    List<Designation> findByApplicableRoleOrApplicableRoleIsNull(String role);
    boolean existsByNameIgnoreCase(String name);
}
