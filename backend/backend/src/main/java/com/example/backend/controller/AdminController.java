package com.example.backend.controller;

import com.example.backend.dto.request.UpdateUserRoleRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.ProjectResponse;
import com.example.backend.service.ProjectService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ApiResponse<String> adminAccess() {
        return new ApiResponse<>("success", "Admin Access Granted", null);
    }

    @PutMapping("/users/{userId}/role")
    public ApiResponse<Void> updateRole(@PathVariable("userId") Long userId,
                                        @RequestBody UpdateUserRoleRequest request) {

        userService.updateUserRole(userId, request.getRole());

        return new ApiResponse<>("success", "User role updated successfully", null);
    }

    @GetMapping("/projects")
    public ApiResponse<List<ProjectResponse>> getAllProjects() {

        return new ApiResponse<>(
                "success",
                "Projects fetched successfully",
                projectService.getAllProjects().getProjects()
        );
    }

    @GetMapping("/projects/manager/{managerId}")
    public ApiResponse<List<ProjectResponse>> getProjectsByManager(@PathVariable("managerId") Long managerId) {
        return new ApiResponse<>("success", "Projects fetched successfully",
                projectService.getProjectsByManager(managerId).getProjects());
    }

    @GetMapping("/projects/paginated")
    public ApiResponse<PaginationResponse<ProjectResponse>> getAllProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "managerId", required = false) Long managerId
    ) {

        return new ApiResponse<>(
                "success",
                "Projects fetched successfully",
                projectService.getAllProjects(page, size, managerId)
        );
    }
}