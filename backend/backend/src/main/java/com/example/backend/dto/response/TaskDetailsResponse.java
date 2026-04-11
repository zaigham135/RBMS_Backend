package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDetailsResponse implements Serializable {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;

    // project info
    private Long projectId;
    private String projectName;

    // manager info
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private String managerPhoto;

    // assigned user info
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private String assignedToPhoto;

    // created by info
    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;

    private List<CommentResponse> comments;
}
