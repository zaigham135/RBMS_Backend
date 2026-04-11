package com.example.backend.service.impl;

import com.example.backend.Entities.Comment;
import com.example.backend.Entities.Project;
import com.example.backend.Entities.Task;
import com.example.backend.Entities.User;
import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.CommentResponse;
import com.example.backend.dto.response.ManagerDashboardStatsResponse;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.TaskCompletionTrendPoint;
import com.example.backend.dto.response.TaskDetailsResponse;
import com.example.backend.dto.response.TaskResponse;
import com.example.backend.enums.Priority;
import com.example.backend.enums.Role;
import com.example.backend.enums.Status;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.event.TaskAssignedEvent;
import com.example.backend.event.TaskDeletedEvent;
import com.example.backend.event.TaskUpdatedEvent;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ActivityLogService;
import com.example.backend.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ActivityLogService activityLogService;
    private User getCurrentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new UnauthorizedException("Invalid authentication");
    }

    private TaskResponse mapToResponse(Task task) {
        return mapToResponse(task, null);
    }

    private TaskResponse mapToResponse(Task task, String taskType) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getPriority() != null ? task.getPriority().name() : null,
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,
                task.getProject() != null && task.getProject().getManager() != null ? task.getProject().getManager().getId() : null,
                task.getProject() != null && task.getProject().getManager() != null ? task.getProject().getManager().getName() : null,
                task.getProject() != null && task.getProject().getManager() != null ? task.getProject().getManager().getEmail() : null,
                task.getProject() != null && task.getProject().getManager() != null ? task.getProject().getManager().getProfilePhoto() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getName() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getProfilePhoto() : null,
                task.getCreatedBy() != null ? task.getCreatedBy().getId() : null,
                task.getCreatedBy() != null ? task.getCreatedBy().getName() : null,
                task.getCreatedAt(),
                taskType
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task-details", allEntries = true)
    })
    @Override
    public void createTask(CreateTaskRequest request) {

        User currentUser = getCurrentUser();

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Task title is required");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if ("MANAGER".equals(currentUser.getRole().name()) &&
                !project.getManager().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        User assignedUser = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));

        if ("INACTIVE".equals(assignedUser.getStatus())) {
            throw new BadRequestException("Cannot assign task to a deactivated user");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(currentUser);
        task.setStatus(Status.TODO);
        task.setDueDate(request.getDueDate());

        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            try {
                task.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority. Allowed values: LOW, MEDIUM, HIGH");
            }
        } else {
            task.setPriority(Priority.MEDIUM);
        }

        taskRepository.save(task);
        log.info("Task created: id={}, title={}, assignedTo={}, project={}", task.getId(), task.getTitle(), assignedUser.getEmail(), project.getName());
        activityLogService.log(currentUser, "created task", "TASK", task.getId(), task.getTitle(), project.getId(), project.getName());

        publisher.publishEvent(new TaskAssignedEvent(assignedUser.getEmail(), task.getTitle(), project.getName()));
    }

//    @Override
//    public List<TaskResponse> getMyTasks() {
//
//        User currentUser = getCurrentUser();
//        String role = currentUser.getRole().name();
//
//        if ("ADMIN".equals(role)) {
//            return taskRepository.findAll()
//                    .stream()
//                    .map(task -> mapToResponse(task, null))
//                    .toList();
//        }
//
//        if ("MANAGER".equals(role)) {
//            return taskRepository.findTasksForManager(currentUser.getId())
//                    .stream()
//                    .map(task -> {
//                        boolean isMyTask = task.getAssignedTo() != null &&
//                                task.getAssignedTo().getId().equals(currentUser.getId());
//                        return mapToResponse(task, isMyTask ? "MY_TASK" : "TEAM_TASK");
//                    })
//                    .toList();
//        }
//
//        return taskRepository.findByAssignedToId(currentUser.getId())
//                .stream()
//                .map(task -> mapToResponse(task, null))
//                .toList();
//    }

//    @Override
//    public List<TaskResponse> getTasksByUser(Long userId) {
//        userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        return taskRepository.findByAssignedToId(userId)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//
//    @Override
//    public List<TaskResponse> getTasksByProject(Long projectId) {
//        projectRepository.findById(projectId)
//                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
//        return taskRepository.findByProjectId(projectId)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

    @Cacheable(
            value = "task-details",
            key = "#taskId + '-' + T(org.springframework.security.core.context.SecurityContextHolder)" +
                    ".getContext().getAuthentication().getName()"
    )
    @Override
    public TaskDetailsResponse getTaskById(Long taskId) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole().name();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if ("EMPLOYEE".equals(role)) {
            boolean isAssigned = task.getAssignedTo() != null &&
                    task.getAssignedTo().getId().equals(currentUser.getId());
            boolean isCreator = task.getCreatedBy() != null &&
                    task.getCreatedBy().getId().equals(currentUser.getId());
            if (!isAssigned && !isCreator) {
                throw new UnauthorizedException("Access denied");
            }
        } else if ("MANAGER".equals(role)) {
            boolean isManagerProject = task.getProject() != null &&
                    task.getProject().getManager() != null &&
                    task.getProject().getManager().getId().equals(currentUser.getId());
            boolean isAssigned = task.getAssignedTo() != null &&
                    task.getAssignedTo().getId().equals(currentUser.getId());
            boolean isCreator = task.getCreatedBy() != null &&
                    task.getCreatedBy().getId().equals(currentUser.getId());
            if (!isManagerProject && !isAssigned && !isCreator) {
                throw new UnauthorizedException("Access denied");
            }
        }

        List<CommentResponse> comments = commentRepository
                .findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getCommentText(),
                        c.getUser().getName(),
                        c.getUser().getProfilePhoto(),
                        c.getCreatedAt()
                ))
                .toList();

        TaskDetailsResponse response = new TaskDetailsResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setPriority(task.getPriority().name());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());

        if (task.getProject() != null) {
            response.setProjectId(task.getProject().getId());
            response.setProjectName(task.getProject().getName());
            if (task.getProject().getManager() != null) {
                response.setManagerId(task.getProject().getManager().getId());
                response.setManagerName(task.getProject().getManager().getName());
                response.setManagerEmail(task.getProject().getManager().getEmail());
                response.setManagerPhoto(task.getProject().getManager().getProfilePhoto());
            }
        }
        if (task.getAssignedTo() != null) {
            response.setAssignedToId(task.getAssignedTo().getId());
            response.setAssignedToName(task.getAssignedTo().getName());
            response.setAssignedToEmail(task.getAssignedTo().getEmail());
            response.setAssignedToPhoto(task.getAssignedTo().getProfilePhoto());
        }
        if (task.getCreatedBy() != null) {
            response.setCreatedById(task.getCreatedBy().getId());
            response.setCreatedByName(task.getCreatedBy().getName());
        }
        response.setComments(comments);

        return response;
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task-details", allEntries = true)
    })
    @Override
    public void updateTaskStatus(Long taskId, UpdateTaskRequest request) {

        // ✅ validate BEFORE doing anything
        if (request.getStatus() == null &&
                request.getDescription() == null &&
                request.getComment() == null &&
                request.getDueDate() == null &&
                (request.getPriority() == null || request.getPriority().isBlank())) {
            throw new BadRequestException("Nothing to update");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = getCurrentUser();
        String role = currentUser.getRole().name();

        Long assignedUserId = task.getAssignedTo() != null
                ? task.getAssignedTo().getId() : null;
        boolean isEmployee = "EMPLOYEE".equals(role);
        boolean isOwner = assignedUserId != null
                && assignedUserId.equals(currentUser.getId());

        if (isEmployee && !isOwner) {
            throw new UnauthorizedException("Access denied");
        }

        if (request.getStatus() != null) {
            try {
                task.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status. Allowed values: TODO, IN_PROGRESS, DONE");
            }
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        // manager-only fields: dueDate and priority
        boolean isManager = "MANAGER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);

        // check if manager owns the project of this task
        boolean isProjectManager = isManager && task.getProject() != null &&
                task.getProject().getManager() != null &&
                task.getProject().getManager().getId().equals(currentUser.getId());

        // check if manager is assigned to this task (acting as employee)
        boolean isAssignedManager = isManager && task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(currentUser.getId());

        // block manager from updating tasks they have no relation to
        if (isManager && !isProjectManager && !isAssignedManager) {
            throw new UnauthorizedException("Access denied");
        }

        // dueDate and priority: only project manager or admin can change
        if (request.getDueDate() != null || (request.getPriority() != null && !request.getPriority().isBlank())) {
            if (!isProjectManager && !isAdmin) {
                throw new UnauthorizedException("Only the project manager or admin can update due date and priority");
            }
            if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
            if (request.getPriority() != null) {
                try {
                    task.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid priority. Allowed: LOW, MEDIUM, HIGH");
                }
            }
        }

        taskRepository.save(task);

        // save comment after task is saved
        if (request.getComment() != null && !request.getComment().isBlank()) {
            Comment comment = new Comment();
            comment.setTask(task);
            comment.setUser(currentUser);
            comment.setCommentText(request.getComment());
            commentRepository.save(comment);
        }

        // notify manager only when an EMPLOYEE updates the task status
        if (isEmployee && request.getStatus() != null && task.getProject().getManager() != null) {
            String managerEmail = task.getProject().getManager().getEmail();
            log.info("Task status updated by employee: taskId={}, updatedBy={}, newStatus={}, notifyingManager={}", taskId, currentUser.getEmail(), task.getStatus(), managerEmail);
            activityLogService.log(currentUser, "updated task status to " + task.getStatus().name(), "TASK", task.getId(), task.getTitle(),
                    task.getProject().getId(), task.getProject().getName());
            publisher.publishEvent(new TaskUpdatedEvent(managerEmail, task.getTitle(), task.getStatus().name()));
        }

        // log manager activity too
        if (!isEmployee && request.getStatus() != null) {
            activityLogService.log(currentUser, "updated task status to " + task.getStatus().name(), "TASK", task.getId(), task.getTitle(),
                    task.getProject() != null ? task.getProject().getId() : null,
                    task.getProject() != null ? task.getProject().getName() : null);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task-details", allEntries = true)
    })
    @Override
    public void deleteTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = getCurrentUser();

        boolean isAdmin = "ADMIN".equals(currentUser.getRole().name());
        boolean isManager = "MANAGER".equals(currentUser.getRole().name());

        if (!isAdmin && !isManager) {
            throw new UnauthorizedException("Access denied");
        }

        if (isManager &&
                !task.getProject().getManager().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        String assignedEmail = task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null;
        String taskTitle = task.getTitle();

        commentRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
        log.info("Task deleted: id={}, title={}, deletedBy={}", taskId, taskTitle, currentUser.getEmail());
        activityLogService.log(currentUser, "deleted task", "TASK", taskId, taskTitle,
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null);

        if (assignedEmail != null) {
            publisher.publishEvent(new TaskDeletedEvent(assignedEmail, taskTitle));
        }
    }

    @Override
    public PaginationResponse<TaskResponse> getTasks(
            int page, int size, Long projectId, String status) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole().name();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.debug("Fetching tasks: page={}, size={}, projectId={}, status={}, role={} (cache miss)", page, size, projectId, status, role);
        Status statusEnum = null;
        if (status != null) {
            try {
                statusEnum = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status. Allowed values: TODO, IN_PROGRESS, DONE");
            }
        }

        Page<Task> taskPage;

        if ("EMPLOYEE".equals(role)) {

            if (projectId != null) {
                taskPage = statusEnum != null
                        ? taskRepository.findByAssignedToIdAndProjectIdAndStatus(currentUser.getId(), projectId, statusEnum, pageable)
                        : taskRepository.findByAssignedToIdAndProjectId(currentUser.getId(), projectId, pageable);
            } else {
                taskPage = statusEnum != null
                        ? taskRepository.findByAssignedToIdAndStatus(currentUser.getId(), statusEnum, pageable)
                        : taskRepository.findByAssignedToId(currentUser.getId(), pageable);
            }

        } else if ("MANAGER".equals(role)) {

            if (projectId != null) {
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                if (!project.getManager().getId().equals(currentUser.getId())) {
                    throw new UnauthorizedException("Access denied");
                }

                taskPage = statusEnum != null
                        ? taskRepository.findByProjectIdAndStatus(projectId, statusEnum, pageable)
                        : taskRepository.findByProjectId(projectId, pageable);

            } else {
                taskPage = statusEnum != null
                        ? taskRepository.findTasksForManagerWithStatus(currentUser.getId(), statusEnum, pageable)
                        : taskRepository.findTasksForManager(currentUser.getId(), pageable);
            }

        } else { // ADMIN

            if (projectId != null && statusEnum != null) {
                taskPage = taskRepository.findByProjectIdAndStatus(projectId, statusEnum, pageable);
            } else if (projectId != null) {
                taskPage = taskRepository.findByProjectId(projectId, pageable);
            } else if (statusEnum != null) {
                taskPage = taskRepository.findByStatus(statusEnum, pageable);
            } else {
                taskPage = taskRepository.findAll(pageable);
            }
        }

        List<TaskResponse> taskList = taskPage.getContent()
                .stream()
                .map(task -> {
                    if ("MANAGER".equals(role)) {
                        boolean isMyTask = task.getAssignedTo() != null &&
                                task.getAssignedTo().getId().equals(currentUser.getId());
                        return mapToResponse(task, isMyTask ? "MY_TASK" : "TEAM_TASK");
                    }
                    return mapToResponse(task);
                })
                .toList();

        return new PaginationResponse<>(
                taskList,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),






                taskPage.getTotalPages()
        );
    }

    @Override
    public ManagerDashboardStatsResponse getManagerDashboardStats() {
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        List<Project> projects = projectRepository.findByManagerId(currentUser.getId());
        int activeProjects = projects.size();

        long pendingTasks = taskRepository.countByManagerIdAndStatus(currentUser.getId(), Status.TODO);

        LocalDateTime startOfWeek = LocalDateTime.now()
            .with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = LocalDateTime.now();
        long completedThisWeek = taskRepository.countByManagerIdAndStatusAndDateRange(
            currentUser.getId(), Status.DONE, startOfWeek, endOfWeek);

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        int teamMembers = employees.size();

        return new ManagerDashboardStatsResponse(
            activeProjects, (int) pendingTasks, (int) completedThisWeek, teamMembers,
            "+0 this month", "+0 this month", "+5%"
        );
    }

    @Override
    public List<TaskCompletionTrendPoint> getCompletionTrend(int days) {
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        LocalDateTime from = LocalDateTime.now().minusDays(days);
        List<Object[]> raw = taskRepository.findCompletionTrendForManager(currentUser.getId(), from);

        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String date = row[0].toString().substring(0, 10);
            countMap.put(date, ((Number) row[1]).intValue());
        }

        List<TaskCompletionTrendPoint> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            result.add(new TaskCompletionTrendPoint(date, countMap.getOrDefault(date, 0)));
        }
        return result;
    }

    @Override
    public PaginationResponse<TaskResponse> getManagerTasks(int page, int size, Long projectId, String status, String priority, Long assignedTo) {
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = Status.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException e) { throw new BadRequestException("Invalid status"); }
        }

        Priority priorityEnum = null;
        if (priority != null && !priority.isBlank()) {
            try { priorityEnum = Priority.valueOf(priority.toUpperCase()); }
            catch (IllegalArgumentException e) { throw new BadRequestException("Invalid priority"); }
        }

        Page<Task> taskPage = taskRepository.findManagerTasksFiltered(
            currentUser.getId(), statusEnum, priorityEnum, projectId, assignedTo, pageable);

        List<TaskResponse> taskList = taskPage.getContent().stream()
            .map(t -> mapToResponse(t, t.getAssignedTo() != null && t.getAssignedTo().getId().equals(currentUser.getId()) ? "MY_TASK" : "TEAM_TASK"))
            .toList();

        return new PaginationResponse<>(taskList, taskPage.getNumber(), taskPage.getSize(),
            taskPage.getTotalElements(), taskPage.getTotalPages());
    }
}