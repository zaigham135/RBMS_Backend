package com.example.backend.repository;

import com.example.backend.Entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByManagerId(Long managerId);
    List<Project> findByManagerIsNotNull();
    Page<Project> findByManagerId(Long managerId, Pageable pageable);
}