package com.example.backend.dto.request;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private Long managerId;
}