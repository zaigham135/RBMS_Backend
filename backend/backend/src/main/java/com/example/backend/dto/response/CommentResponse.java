package com.example.backend.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentResponse implements Serializable {

    private Long id;
    private String commentText;
    private String userName;
    private String userPhoto;
    private LocalDateTime createdAt;

    public CommentResponse(Long id, String commentText, String userName, String userPhoto, LocalDateTime createdAt) {
        this.id = id;
        this.commentText = commentText;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.createdAt = createdAt;
    }
}
