package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String userName;
    private String userPhoto;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private Long projectId;
    private String projectName;
    private LocalDateTime createdAt;
}
