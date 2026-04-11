package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String profilePhoto;
    private String role;
}
