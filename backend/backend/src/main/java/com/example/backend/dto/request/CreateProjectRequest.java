package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private Long managerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dueDate;
}