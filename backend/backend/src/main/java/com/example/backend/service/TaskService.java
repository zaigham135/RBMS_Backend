package com.example.backend.service;

import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.ManagerDashboardStatsResponse;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.TaskCompletionTrendPoint;
import com.example.backend.dto.response.TaskDetailsResponse;
import com.example.backend.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {
    void createTask(CreateTaskRequest request);
//    List<TaskResponse> getMyTasks();
//    List<TaskResponse> getTasksByUser(Long userId);
//    List<TaskResponse> getTasksByProject(Long projectId);
    void updateTaskStatus(Long taskId, UpdateTaskRequest request);
    void deleteTask(Long taskId);
//    TaskResponse getTaskById(Long taskId);
    TaskDetailsResponse getTaskById(Long taskId);
    PaginationResponse<TaskResponse> getTasks(
            int page,
            int size,
            Long projectId,
            String status
    );
    List<TaskCompletionTrendPoint> getCompletionTrend(int days);
    PaginationResponse<TaskResponse> getManagerTasks(int page, int size, Long projectId, String status, String priority, Long assignedTo);
    ManagerDashboardStatsResponse getManagerDashboardStats();
}