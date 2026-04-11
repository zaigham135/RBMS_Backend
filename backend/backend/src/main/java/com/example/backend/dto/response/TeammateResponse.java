package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeammateResponse {
    private Long id;
    private String name;
    private String email;
    private String profilePhoto;
    private String projectName;
    private String latestTaskTitle;
    private String latestTaskStatus;
    private String latestTaskDueDate; // "yyyy-MM-dd"
}
