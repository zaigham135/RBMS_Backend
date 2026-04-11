package com.example.backend.controller;

import com.example.backend.Entities.Task;
import com.example.backend.Entities.User;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ProjectResponse;
import com.example.backend.dto.response.TeammateResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> getProfile() {
        User user = getCurrentUser();
        UserResponse response = new UserResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), user.getStatus(),
                user.getProfilePhoto(), user.getCreatedAt(), user.getDesignation()
        );
        return new ApiResponse<>("success", "Profile fetched", response);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile() {
        throw new UnauthorizedException("You do not have access to edit profile details. Contact admin.");
    }

    /** Returns distinct projects the current employee has tasks assigned in */
    @GetMapping("/my-projects")
    public ApiResponse<List<ProjectResponse>> getMyProjects() {
        User currentUser = getCurrentUser();
        List<ProjectResponse> projects = projectRepository
                .findProjectsByAssignedUserId(currentUser.getId())
                .stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getManager() != null ? p.getManager().getId() : null,
                        p.getManager() != null ? p.getManager().getName() : null,
                        p.getManager() != null ? p.getManager().getEmail() : null,
                        p.getManager() != null ? p.getManager().getProfilePhoto() : null,
                        p.getDueDate()
                ))
                .toList();
        return new ApiResponse<>("success", "Projects fetched successfully", projects);
    }

    /** Returns teammates (users sharing the same projects) with their latest task info */
    @GetMapping("/teammates")
    public ApiResponse<List<TeammateResponse>> getTeammates() {
        User currentUser = getCurrentUser();
        List<User> teammates = userRepository.findTeammatesByUserId(currentUser.getId());

        List<TeammateResponse> result = new ArrayList<>();
        for (User teammate : teammates) {
            var tasks = taskRepository.findByAssignedToId(
                    teammate.getId(),
                    PageRequest.of(0, 1, Sort.by("createdAt").descending())
            );
            String projectName = "";
            String taskTitle = "";
            String taskStatus = "";
            String taskDueDate = "";
            if (!tasks.isEmpty()) {
                Task latest = tasks.getContent().get(0);
                projectName = latest.getProject() != null ? latest.getProject().getName() : "";
                taskTitle = latest.getTitle();
                taskStatus = latest.getStatus() != null ? latest.getStatus().name() : "";
                taskDueDate = latest.getDueDate() != null ? latest.getDueDate().toString() : "";
            }
            result.add(new TeammateResponse(
                    teammate.getId(), teammate.getName(), teammate.getEmail(),
                    teammate.getProfilePhoto(), projectName, taskTitle, taskStatus, taskDueDate
            ));
        }
        return new ApiResponse<>("success", "Teammates fetched", result);
    }
}
