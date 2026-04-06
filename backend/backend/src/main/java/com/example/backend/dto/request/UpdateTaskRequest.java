package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String status;
    private String description;
    private String comment;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dueDate;
    private String priority;
}