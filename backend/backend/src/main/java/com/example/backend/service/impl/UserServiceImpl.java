package com.example.backend.service.impl;

import com.example.backend.Entities.Project;
import com.example.backend.Entities.User;
import com.example.backend.dto.response.EmployeeWithWorkloadResponse;
import com.example.backend.dto.response.ManagerTeamStatsResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.enums.Role;
import com.example.backend.enums.Status;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new UnauthorizedException("Invalid authentication");
    }

    private UserResponse mapToResponse(User u) {
        UserResponse r = new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getRole().name(), u.getStatus(), u.getProfilePhoto(), u.getCreatedAt(), u.getDesignation());
        return r;
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        log.info("Updating role: userId={}, newRole={}", userId, role);
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) {
            log.warn("Unauthorized role update attempt by userId={}", currentUser.getId());
            throw new UnauthorizedException("Only admin can update roles");
        }
        if (role == null || role.isBlank()) throw new BadRequestException("Role cannot be empty");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role provided: role={}", role);
            throw new BadRequestException("Invalid role: " + role + ". Allowed: ADMIN, MANAGER, EMPLOYEE");
        }
        userRepository.save(user);
        log.info("Role updated: userId={}, role={}", userId, role.toUpperCase());
    }

    @Override
    public List<UserResponse> getAllEmployees() {
        log.debug("Fetching all employees/managers");
        User currentUser = getCurrentUser();
        String role = currentUser.getRole().name();
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new UnauthorizedException("Access denied");
        }
        List<UserResponse> users;
        if ("ADMIN".equals(role)) {
            // admin sees both employees and managers
            users = userRepository.findByRole(Role.EMPLOYEE).stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
            users.addAll(userRepository.findByRole(Role.MANAGER).stream().map(this::mapToResponse).toList());
        } else {
            // manager sees only employees
            users = userRepository.findByRole(Role.EMPLOYEE).stream().map(this::mapToResponse).toList();
        }
        log.debug("Fetched {} users", users.size());
        return users;
    }

    @Override
    public void setUserStatus(Long userId, String status) {
        log.info("Updating status: userId={}, status={}", userId, status);
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Only admin can activate/deactivate users");
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new BadRequestException("Invalid status. Allowed: ACTIVE, INACTIVE");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setStatus(status);
        userRepository.save(user);
        log.info("User status updated: userId={}, status={}", userId, status);
    }

    @Override
    public void updateUserProfile(Long userId, String name) {
        log.info("Admin updating profile: userId={}, name={}", userId, name);
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Only admin can update user profiles");
        }
        if (name == null || name.isBlank()) throw new BadRequestException("Name cannot be empty");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setName(name);
        userRepository.save(user);
        log.info("Profile updated: userId={}, name={}", userId, name);
    }

    @Override
    public List<EmployeeWithWorkloadResponse> getEmployeesWithWorkload() {
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        List<Project> managerProjects = projectRepository.findByManagerId(currentUser.getId());
        List<Long> projectIds = managerProjects.stream().map(Project::getId).toList();

        return employees.stream().map(emp -> {
            long inProgressCount = taskRepository.countByAssignedToIdAndStatus(emp.getId(), Status.IN_PROGRESS);
            int workload = (int) Math.min(100, (inProgressCount * 10));
            List<String> activeProjects = taskRepository.findByAssignedToId(emp.getId(), PageRequest.of(0, 3))
                .getContent().stream()
                .filter(t -> t.getProject() != null && projectIds.contains(t.getProject().getId()))
                .map(t -> t.getProject().getName())
                .distinct()
                .limit(3)
                .toList();
            return new EmployeeWithWorkloadResponse(
                emp.getId(), emp.getName(), emp.getEmail(), emp.getRole().name(),
                emp.getStatus(), emp.getProfilePhoto(), emp.getCreatedAt(),
                workload, emp.getDepartment() != null ? emp.getDepartment() : "General",
                activeProjects
            );
        }).toList();
    }

    @Override
    public ManagerTeamStatsResponse getManagerTeamStats() {
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        int total = employees.size();

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();

        int tasksCompletedThisMonth = 0;
        int overloaded = 0;
        double totalWorkload = 0;

        for (User emp : employees) {
            long inProgress = taskRepository.countByAssignedToIdAndStatus(emp.getId(), Status.IN_PROGRESS);
            int workload = (int) Math.min(100, inProgress * 10);
            totalWorkload += workload;
            if (workload >= 85) overloaded++;
            tasksCompletedThisMonth += (int) taskRepository.countCompletedThisMonthForUser(emp.getId(), startOfMonth, endOfMonth);
        }

        double avgWorkload = total > 0 ? totalWorkload / total : 0;
        return new ManagerTeamStatsResponse(total, Math.round(avgWorkload * 10.0) / 10.0, tasksCompletedThisMonth, overloaded, "+0 this month");
    }
}