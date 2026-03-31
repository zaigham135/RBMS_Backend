package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;

    private List<CommentResponse> comments;
}
