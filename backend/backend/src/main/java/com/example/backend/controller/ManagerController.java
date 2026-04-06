package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ProjectResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.ProjectService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<String> managerAccess() {
        return new ApiResponse<>("success", "Manager Access Granted", null);
    }

    @GetMapping("/my-projects")
    public ApiResponse<List<ProjectResponse>> getMyProjects() {
        return new ApiResponse<>("success", "Projects fetched successfully",
                projectService.getProjectsForManager().getProjects());
    }

    @GetMapping("/employees")
    public ApiResponse<List<UserResponse>> getAllEmployees() {
        return new ApiResponse<>("success", "Employees fetched successfully",
                userService.getAllEmployees());
    }
}