package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private String managerPhoto;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dueDate;
    private String status; // ACTIVE or ON_HOLD
    private List<ProjectMemberResponse> members;

    // Backward-compat constructor without members
    public ProjectResponse(Long id, String name, String description,
                           Long managerId, String managerName, String managerEmail,
                           String managerPhoto, java.time.LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.managerPhoto = managerPhoto;
        this.dueDate = dueDate;
        this.members = new java.util.ArrayList<>();
    }
}