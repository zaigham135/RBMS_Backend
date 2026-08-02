package com.example.backend.controller;

import com.example.backend.dto.request.UpdateUserRoleRequest;
import com.example.backend.dto.response.*;
import com.example.backend.Entities.Designation;
import com.example.backend.enums.Role;
import com.example.backend.enums.Status;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.DesignationRepository;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ActivityLogService;
import com.example.backend.service.ProjectService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private ActivityLogService activityLogService;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private DesignationRepository designationRepository;
    @Autowired private com.example.backend.service.EmailService emailSvc;

    @GetMapping
    public ApiResponse<String> adminAccess() {
        return new ApiResponse<>("success", "Admin Access Granted", null);
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<DashboardStatsResponse> getDashboardStats() {
        long totalProjects = projectRepository.count();
        long openTasks = taskRepository.countByStatus(Status.TODO) + taskRepository.countByStatus(Status.IN_PROGRESS);
        long doneTasks = taskRepository.countByStatus(Status.DONE);
        long activeUsers = userRepository.countByStatus("ACTIVE");
        long totalManagers = userRepository.countByRole(Role.MANAGER);
        long totalEmployees = userRepository.countByRole(Role.EMPLOYEE);
        return new ApiResponse<>("success", "Dashboard stats fetched",
                new DashboardStatsResponse(totalProjects, openTasks, doneTasks, activeUsers, totalManagers, totalEmployees));
    }

    @GetMapping("/dashboard/activity")
    public ApiResponse<List<ActivityLogResponse>> getRecentActivity(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<ActivityLogResponse> activities = activityLogService.getRecent(limit)
                .stream()
                .map(a -> new ActivityLogResponse(
                        a.getId(),
                        a.getUser() != null ? a.getUser().getName() : "System",
                        a.getUser() != null ? a.getUser().getProfilePhoto() : null,
                        a.getAction(),
                        a.getEntityType(),
                        a.getEntityId(),
                        a.getEntityName(),
                        a.getProjectId(),
                        a.getProjectName(),
                        a.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return new ApiResponse<>("success", "Activity log fetched", activities);
    }

    @PutMapping("/users/{userId}/profile")
    public ApiResponse<Void> updateUserProfile(@PathVariable("userId") Long userId,
                                               @RequestBody Map<String, String> body) {
        userService.updateUserProfile(userId, body.get("name"));
        return new ApiResponse<>("success", "User profile updated successfully", null);
    }

    @PutMapping("/users/{userId}/role")
    public ApiResponse<Void> updateRole(@PathVariable("userId") Long userId,
                                        @RequestBody UpdateUserRoleRequest request) {
        userService.updateUserRole(userId, request.getRole());
        return new ApiResponse<>("success", "User role updated successfully", null);
    }

    @GetMapping("/employees")
    public ApiResponse<List<UserResponse>> getAllEmployees() {
        return new ApiResponse<>("success", "Employees fetched successfully", userService.getAllEmployees());
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable("userId") Long userId,
                                              @RequestBody Map<String, String> body) {
        userService.setUserStatus(userId, body.get("status"));
        return new ApiResponse<>("success", "User status updated successfully", null);
    }

    @GetMapping("/projects")
    public ApiResponse<List<ProjectResponse>> getAllProjects() {
        return new ApiResponse<>("success", "Projects fetched successfully",
                projectService.getAllProjects().getProjects());
    }

    @GetMapping("/projects/manager/{managerId}")
    public ApiResponse<List<ProjectResponse>> getProjectsByManager(@PathVariable("managerId") Long managerId) {
        return new ApiResponse<>("success", "Projects fetched successfully",
                projectService.getProjectsByManager(managerId).getProjects());
    }

    @GetMapping("/projects/paginated")
    public ApiResponse<PaginationResponse<ProjectResponse>> getAllProjectsPaginated(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "managerId", required = false) Long managerId) {
        return new ApiResponse<>("success", "Projects fetched successfully",
                projectService.getAllProjects(page, size, managerId));
    }

    // ── Designation endpoints ──────────────────────────────────────────────────

    @GetMapping("/designations")
    public ApiResponse<List<DesignationResponse>> getAllDesignations() {
        List<DesignationResponse> list = designationRepository.findAll().stream()
                .map(d -> new DesignationResponse(d.getId(), d.getName(), d.getApplicableRole()))
                .toList();
        return new ApiResponse<>("success", "Designations fetched", list);
    }

    @PostMapping("/designations")
    public ApiResponse<DesignationResponse> createDesignation(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String applicableRole = body.get("applicableRole"); // MANAGER, EMPLOYEE, or null
        if (name == null || name.isBlank()) throw new BadRequestException("Designation name is required");
        if (designationRepository.existsByNameIgnoreCase(name.trim()))
            throw new BadRequestException("Designation '" + name + "' already exists");
        Designation d = designationRepository.save(new Designation(name.trim(), applicableRole));
        return new ApiResponse<>("success", "Designation created", new DesignationResponse(d.getId(), d.getName(), d.getApplicableRole()));
    }

    @DeleteMapping("/designations/{id}")
    public ApiResponse<Void> deleteDesignation(@PathVariable Long id) {
        Designation d = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));
        // Clear designation from users who have it
        userRepository.findAll().stream()
                .filter(u -> d.getName().equals(u.getDesignation()))
                .forEach(u -> { u.setDesignation(null); userRepository.save(u); });
        designationRepository.delete(d);
        return new ApiResponse<>("success", "Designation deleted", null);
    }

    @PutMapping("/users/{userId}/designation")
    public ApiResponse<Void> updateUserDesignation(@PathVariable Long userId,
                                                    @RequestBody Map<String, String> body) {
        com.example.backend.Entities.User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN)
            throw new BadRequestException("Cannot assign designation to admin users");
        String oldDesignation = user.getDesignation();
        String designation = body.get("designation");
        String newDesignation = (designation == null || designation.isBlank()) ? null : designation.trim();
        user.setDesignation(newDesignation);
        userRepository.save(user);

        // Send email notification
        String prevLabel = oldDesignation != null ? oldDesignation : "None";
        String newLabel  = newDesignation  != null ? newDesignation  : "None";
        String content = emailSvc.buildInfoBox(
            "Account",              "<strong>" + user.getName() + "</strong>",
            "Email",                user.getEmail(),
            "Previous Designation", prevLabel,
            "New Designation",      newDesignation != null ? emailSvc.buildBadge(newDesignation, "#a78bfa") : "<em>Cleared</em>"
        );
        String html = emailSvc.buildHtml(
            newDesignation != null ? "Your designation has been updated" : "Your designation has been cleared",
            newDesignation != null
                ? "An administrator has assigned you a new designation on the TaskMan platform."
                : "An administrator has removed your designation on the TaskMan platform.",
            content,
            "If you believe this change was made in error, please contact your system administrator."
        );
        emailSvc.sendTaskUpdateEmail(user.getEmail(), "Designation Updated — TaskMan", html);

        return new ApiResponse<>("success", "Designation updated", null);
    }
}
