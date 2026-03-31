package com.example.backend.dto.request;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String status;
    private String description;
    private String comment;
}