package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor

//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class ProjectResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private String managerEmail;
}