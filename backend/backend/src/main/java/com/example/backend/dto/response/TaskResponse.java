package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class TaskResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;

    // project info
    private Long projectId;
    private String projectName;

    // assigned user info
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // created by info
    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private String taskType;

}