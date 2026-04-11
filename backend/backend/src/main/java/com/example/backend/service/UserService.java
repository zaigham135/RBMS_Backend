package com.example.backend.service;

import com.example.backend.dto.response.EmployeeWithWorkloadResponse;
import com.example.backend.dto.response.ManagerTeamStatsResponse;
import com.example.backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    void updateUserRole(Long userId, String role);
    List<UserResponse> getAllEmployees();
    void setUserStatus(Long userId, String status);
    void updateUserProfile(Long userId, String name);
    List<EmployeeWithWorkloadResponse> getEmployeesWithWorkload();
    ManagerTeamStatsResponse getManagerTeamStats();
}
