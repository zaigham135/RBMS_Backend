package com.example.backend.controller;

import com.example.backend.Entities.ActivityLog;
import com.example.backend.dto.response.*;
import com.example.backend.service.ActivityLogService;
import com.example.backend.service.ProjectService;
import com.example.backend.service.TaskService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private TaskService taskService;
    @Autowired private ActivityLogService activityLogService;

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
    public ApiResponse<List<EmployeeWithWorkloadResponse>> getAllEmployees() {
        return new ApiResponse<>("success", "Employees fetched successfully",
                userService.getEmployeesWithWorkload());
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<ManagerDashboardStatsResponse> getDashboardStats() {
        return new ApiResponse<>("success", "Stats fetched successfully",
                taskService.getManagerDashboardStats());
    }

    @GetMapping("/dashboard/activity")
    public ApiResponse<List<ActivityLogResponse>> getDashboardActivity(
            @RequestParam(defaultValue = "10") int limit) {
        List<Long> projectIds = projectService.getProjectsForManager().getProjects()
                .stream().map(ProjectResponse::getId).toList();
        List<ActivityLog> logs = activityLogService.getRecentForProjects(projectIds, limit);
        List<ActivityLogResponse> response = logs.stream().map(a -> new ActivityLogResponse(
                a.getId(),
                a.getUser() != null ? a.getUser().getName() : "Unknown",
                a.getUser() != null ? a.getUser().getProfilePhoto() : null,
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getEntityName(),
                a.getProjectId(),
                a.getProjectName(),
                a.getCreatedAt()
        )).toList();
        return new ApiResponse<>("success", "Activity fetched successfully", response);
    }

    @DeleteMapping("/dashboard/activity/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityLogService.deleteById(id);
        return new ApiResponse<>("success", "Activity deleted successfully", null);
    }

    @GetMapping("/tasks/completion-trend")    public ApiResponse<List<TaskCompletionTrendPoint>> getCompletionTrend(
            @RequestParam(defaultValue = "30") int days) {
        return new ApiResponse<>("success", "Trend fetched successfully",
                taskService.getCompletionTrend(days));
    }

    @GetMapping("/tasks")
    public ApiResponse<PaginationResponse<TaskResponse>> getManagerTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedTo) {
        return new ApiResponse<>("success", "Tasks fetched successfully",
                taskService.getManagerTasks(page, size, projectId, status, priority, assignedTo));
    }

    @GetMapping("/team/stats")
    public ApiResponse<ManagerTeamStatsResponse> getTeamStats() {
        return new ApiResponse<>("success", "Team stats fetched successfully",
                userService.getManagerTeamStats());
    }
}
