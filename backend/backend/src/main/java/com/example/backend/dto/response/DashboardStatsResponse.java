package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalProjects;
    private long openTasks;
    private long doneTasks;
    private long activeUsers;
    private long totalManagers;
    private long totalEmployees;
}
