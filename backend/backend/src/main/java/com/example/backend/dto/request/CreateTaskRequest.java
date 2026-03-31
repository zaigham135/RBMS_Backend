package com.example.backend.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private Long projectId;
    private Long assignedTo;
    private LocalDate dueDate;
    private String priority;
}