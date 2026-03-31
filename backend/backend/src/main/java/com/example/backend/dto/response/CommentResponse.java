package com.example.backend.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

public class CommentResponse {

    private Long id;
    private String commentText;
    private String userName;
    private LocalDateTime createdAt;

    public CommentResponse(Long id, String commentText, String userName, LocalDateTime createdAt) {
        this.id = id;
        this.commentText = commentText;
        this.userName = userName;
        this.createdAt = createdAt;
    }
}
