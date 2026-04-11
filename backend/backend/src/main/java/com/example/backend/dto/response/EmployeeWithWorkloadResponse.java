package com.example.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithWorkloadResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private int workloadPercent;
    private String department;
    private List<String> activeProjects;
}
