package com.example.backend.controller;

import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.TaskDetailsResponse;
import com.example.backend.dto.response.TaskResponse;
import com.example.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ApiResponse<Void> createTask(@RequestBody CreateTaskRequest request) {
        taskService.createTask(request);
        return new ApiResponse<>("success", "Task created successfully", null);
    }

//    @GetMapping("/my-tasks")
//    public ApiResponse<List<TaskResponse>> getMyTasks() {
//        return new ApiResponse<>("success", "Tasks fetched successfully", taskService.getMyTasks());
//    }
//
//    @GetMapping("/user/{userId}")
//    public ApiResponse<List<TaskResponse>> getTasksByUser(@PathVariable Long userId) {
//        return new ApiResponse<>("success", "Tasks fetched successfully",
//                taskService.getTasksByUser(userId));
//    }

//    @GetMapping("/project/{projectId}")
//    public ApiResponse<List<TaskResponse>> getTasksByProject(@PathVariable Long projectId) {
//        return new ApiResponse<>("success", "Tasks fetched successfully",
//                taskService.getTasksByProject(projectId));
//    }

    @PutMapping("/{taskId}")
    public ApiResponse<Void> updateTask(@PathVariable Long taskId,
                                        @RequestBody UpdateTaskRequest request) {
        taskService.updateTaskStatus(taskId, request);
        return new ApiResponse<>("success", "Task updated successfully", null);
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return new ApiResponse<>("success", "Task deleted successfully", null);
    }
    @GetMapping
    public ApiResponse<PaginationResponse<TaskResponse>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status
    ) {

        return new ApiResponse<>(
                "success",
                "Tasks fetched successfully",
                taskService.getTasks(page, size, projectId, status)
        );
    }
//    @GetMapping("/{taskId}")
//    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long taskId) {
//
//        return new ApiResponse<>(
//                "success",
//                "Task fetched successfully",
//                taskService.getTaskById(taskId)
//        );
//    }
    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailsResponse> getTaskById(@PathVariable Long taskId) {

        return new ApiResponse<>(
                "success",
                "Task fetched successfully",
                taskService.getTaskById(taskId)
        );
    }
}