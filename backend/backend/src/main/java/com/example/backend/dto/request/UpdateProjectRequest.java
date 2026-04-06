package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dueDate;
}