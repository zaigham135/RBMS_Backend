package com.example.backend.service.impl;

import com.example.backend.Entities.Comment;
import com.example.backend.Entities.Project;
import com.example.backend.Entities.Task;
import com.example.backend.Entities.User;
import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.CommentResponse;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.TaskDetailsResponse;
import com.example.backend.dto.response.TaskResponse;
import com.example.backend.enums.Priority;
import com.example.backend.enums.Status;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    // ✅ get current logged-in user
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
    // ✅ convert Task entity → TaskResponse DTO
    private TaskResponse mapToResponse(Task task,String taskType) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getPriority() != null ? task.getPriority().name() : null,
                task.getDueDate(),

                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,

                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getName() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null,

                task.getCreatedBy() != null ? task.getCreatedBy().getId() : null,
                task.getCreatedBy() != null ? task.getCreatedBy().getName() : null,
                task.getCreatedAt(),
                taskType
        );
    }

    @Override
    public void createTask(CreateTaskRequest request) {

        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Manager can only create tasks in their own project
        if (currentUser.getRole().name().equals("MANAGER") &&
                !project.getManager().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only create tasks in your project");
        }

        User assignedUser = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(currentUser);  // ✅ set createdBy
        task.setStatus(Status.TODO);
        task.setDueDate(request.getDueDate());

        if (request.getPriority() != null && !request.getPriority().isEmpty()) {
            try {
                task.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new com.example.backend.exception.BadRequestException(
                        "Invalid priority. Allowed: LOW, MEDIUM, HIGH"
                );
            }
        } else {
            task.setPriority(Priority.MEDIUM);
        }

        taskRepository.save(task);
    }

//    @Override
//    public List<TaskResponse> getMyTasks() {
//
//        User currentUser = getCurrentUser();
//        String role = currentUser.getRole().name();
//
//        // ADMIN — sees all tasks
//        if ("ADMIN".equals(role)) {
//            return taskRepository.findAll()
//                    .stream()
//                    .map(task -> mapToResponse(task, null))
//                    .toList();
//        }
//
//        // MANAGER — sees all tasks in their projects + their own tasks
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
//        // EMPLOYEE — sees only their own tasks
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
//
@Override
public TaskDetailsResponse getTaskById(Long taskId) {

    User currentUser = getCurrentUser();
    String role = currentUser.getRole().name();

    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

    // 🔐 ACCESS CONTROL
    if ("EMPLOYEE".equals(role)) {

        if (task.getAssignedTo() == null ||
                !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

    } else if ("MANAGER".equals(role)) {

        boolean isManagerProject =
                task.getProject().getManager().getId().equals(currentUser.getId());

        boolean isAssigned =
                task.getAssignedTo() != null &&
                        task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isManagerProject && !isAssigned) {
            throw new UnauthorizedException("Access denied");
        }
    }

    // =========================
    // 💬 FETCH COMMENTS (ONLY HERE)
    // =========================

    List<CommentResponse> comments = commentRepository
            .findByTaskIdOrderByCreatedAtDesc(taskId)
            .stream()
            .map(c -> new CommentResponse(
                    c.getId(),
                    c.getCommentText(),
                    c.getUser().getName(),
                    c.getCreatedAt()
            ))
            .toList();

    // =========================
    // 📦 RESPONSE
    // =========================

    TaskDetailsResponse response = new TaskDetailsResponse();

    response.setId(task.getId());
    response.setTitle(task.getTitle());
    response.setDescription(task.getDescription());
    response.setStatus(task.getStatus().name());
    response.setPriority(task.getPriority().name());
    response.setComments(comments);

    return response;
}
    @Override
    public void updateTaskStatus(Long taskId, UpdateTaskRequest request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = getCurrentUser();

        String role = currentUser.getRole().name();

        Long assignedUserId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;

        boolean isEmployee = "EMPLOYEE".equals(role);
        boolean isOwner = assignedUserId != null && assignedUserId.equals(currentUser.getId());

        // 🔐 ACCESS CONTROL
        if (isEmployee && !isOwner) {
            throw new UnauthorizedException("You can only update your own tasks");
        }

        // =========================
        // ✅ UPDATE STATUS
        // =========================
        if (request.getStatus() != null) {
            try {
                task.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Invalid status. Allowed: TODO, IN_PROGRESS, DONE"
                );
            }
        }

        // =========================
        // ✅ UPDATE DESCRIPTION
        // =========================
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        taskRepository.save(task);

        // =========================
        // 💬 ADD COMMENT (NEW)
        // =========================
        if (request.getComment() != null && !request.getComment().trim().isEmpty()) {

            Comment comment = new Comment();
            comment.setTask(task);
            comment.setUser(currentUser);
            comment.setCommentText(request.getComment());

            commentRepository.save(comment);
        }
        if (request.getStatus() == null &&
                request.getDescription() == null &&
                request.getComment() == null) {

            throw new BadRequestException("Nothing to update");
        }
    }


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

        // Manager can only delete tasks in their own project
        if (isManager &&
                !task.getProject().getManager().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete tasks in your project");
        }

        taskRepository.delete(task);
    }
    @Override
    public PaginationResponse<TaskResponse> getTasks(
            int page,
            int size,
            Long projectId,
            String status
    ) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole().name();

        Pageable pageable = PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by("createdAt").descending()
        );

        Page<Task> taskPage;

        // 🔥 Convert status safely
        Status statusEnum = null;
        if (status != null) {
            try {
                statusEnum = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Invalid status. Allowed: TODO, IN_PROGRESS, DONE"
                );
            }
        }

        // =========================
        // 🔐 ROLE BASED LOGIC
        // =========================

        if ("EMPLOYEE".equals(role)) {

            if (statusEnum != null) {
                taskPage = taskRepository.findByAssignedToIdAndStatus(
                        currentUser.getId(),
                        statusEnum,
                        pageable
                );
            } else {
                taskPage = taskRepository.findByAssignedToId(
                        currentUser.getId(),
                        pageable
                );
            }


        } else if ("MANAGER".equals(role)) {

            if (projectId != null) {

                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                if (!project.getManager().getId().equals(currentUser.getId())) {
                    throw new UnauthorizedException("Access denied to this project");
                }

                if (statusEnum != null) {
                    taskPage = taskRepository.findByProjectIdAndStatus(projectId, statusEnum, pageable);
                } else {
                    taskPage = taskRepository.findByProjectId(projectId, pageable);
                }

            } else {
                // 🔥 FIX HERE
                if (statusEnum != null) {
                    taskPage = taskRepository.findTasksForManagerWithStatus(
                            currentUser.getId(),
                            statusEnum,
                            pageable
                    );
                } else {
                    taskPage = taskRepository.findTasksForManager(currentUser.getId(), pageable);
                }
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

        // =========================
        // 🔄 DTO MAPPING
        // =========================

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

        // =========================
        // 📦 RESPONSE
        // =========================

        return new PaginationResponse<>(
                taskList,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages()
        );
    }
}