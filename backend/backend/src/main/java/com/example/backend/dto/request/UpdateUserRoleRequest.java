package com.example.backend.dto.request;

import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private String role; // ADMIN / MANAGER / EMPLOYEE
}