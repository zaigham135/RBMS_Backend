package com.example.backend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardStatsResponse {
    private int activeProjects;
    private int pendingTasks;
    private int completedThisWeek;
    private int teamMembers;
    private String activeProjectsTrend;
    private String teamMembersTrend;
    private String completedThisWeekTrend;
}
