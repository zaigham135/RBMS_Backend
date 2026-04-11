package com.example.backend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerTeamStatsResponse {
    private int totalMembers;
    private double avgWorkload;
    private int tasksCompletedThisMonth;
    private int overloadedMembers;
    private String totalMembersTrend;
}
