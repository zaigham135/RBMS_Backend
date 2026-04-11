package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private String designation;
}
