package com.example.backend.service;

import com.example.backend.dto.request.CreateProjectRequest;
import com.example.backend.dto.request.UpdateProjectRequest;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    void createProject(CreateProjectRequest request);
    void updateProject(Long projectId, UpdateProjectRequest request);
    void deleteProject(Long projectId);
    List<ProjectResponse> getAllProjects();
    List<ProjectResponse> getProjectsForManager();
    List<ProjectResponse> getProjectsByManager(Long managerId);
    PaginationResponse<ProjectResponse> getAllProjects(int page, int size, Long managerId);

}