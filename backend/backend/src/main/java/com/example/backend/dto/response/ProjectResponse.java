package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
}