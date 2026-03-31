package com.example.backend.controller;

import com.example.backend.dto.request.CreateProjectRequest;
import com.example.backend.dto.request.UpdateProjectRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ApiResponse<Void> createProject(@RequestBody CreateProjectRequest request) {
        projectService.createProject(request);
        return new ApiResponse<>("success", "Project created successfully", null);
    }

    @PutMapping("/{projectId}")
    public ApiResponse<Void> updateProject(@PathVariable Long projectId,
                                           @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(projectId, request);
        return new ApiResponse<>("success", "Project updated successfully", null);
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return new ApiResponse<>("success", "Project deleted successfully", null);
    }
}