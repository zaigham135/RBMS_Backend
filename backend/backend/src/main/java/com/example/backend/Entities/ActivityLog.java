package com.example.backend.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action; // e.g. "created project", "updated task status"

    @Column(name = "entity_type")
    private String entityType; // PROJECT, TASK, USER

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name")
    private String entityName; // project name, task title etc.

    @Column(name = "project_name")
    private String projectName; // project context for task activities

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
