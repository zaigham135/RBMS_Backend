package com.example.backend.service.impl;

import com.example.backend.Entities.Project;
import com.example.backend.Entities.User;
import com.example.backend.dto.request.CreateProjectRequest;
import com.example.backend.dto.request.UpdateProjectRequest;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.ProjectListResponse;
import com.example.backend.dto.response.ProjectMemberResponse;
import com.example.backend.dto.response.ProjectResponse;
import com.example.backend.event.ProjectAssignedEvent;
import com.example.backend.event.ProjectDeletedEvent;
import com.example.backend.event.ProjectUpdatedEvent;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ActivityLogService;
import com.example.backend.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.backend.repository.TaskRepository taskRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ActivityLogService activityLogService;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new UnauthorizedException("Invalid authentication");
    }

    private ProjectResponse mapToResponse(Project p) {
        java.util.List<ProjectMemberResponse> members = taskRepository
                .findDistinctAssigneesByProjectId(p.getId())
                .stream()
                .map(u -> new ProjectMemberResponse(
                        u.getId(), u.getName(), u.getEmail(),
                        u.getProfilePhoto(), u.getRole() != null ? u.getRole().name() : null
                ))
                .collect(java.util.stream.Collectors.toList());

        ProjectResponse response = new ProjectResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getManager() != null ? p.getManager().getId() : null,
                p.getManager() != null ? p.getManager().getName() : null,
                p.getManager() != null ? p.getManager().getEmail() : null,
                p.getManager() != null ? p.getManager().getProfilePhoto() : null,
                p.getDueDate()
        );
        response.setStatus(p.getStatus() != null ? p.getStatus() : "ACTIVE");
        response.setMembers(members);
        return response;
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Override
    public void createProject(CreateProjectRequest request) {
        log.info("Creating project: name={}", request.getName());

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Project name is required");
        }

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (!"MANAGER".equals(manager.getRole().name())) {
            log.warn("User id={} is not a manager, role={}", manager.getId(), manager.getRole());
            throw new BadRequestException("Assigned user is not a manager");
        }

        if ("INACTIVE".equals(manager.getStatus())) {
            throw new BadRequestException("Cannot assign project to a deactivated manager");
        }

        long projectCount = projectRepository.countByManagerId(manager.getId());
        if (projectCount >= 3) {
            throw new BadRequestException("Manager already has 3 projects. Maximum limit reached");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setManager(manager);
        if (request.getDueDate() != null) project.setDueDate(request.getDueDate());

        projectRepository.save(project);
        log.info("Project created: id={}, name={}, managerId={}", project.getId(), project.getName(), manager.getId());
        activityLogService.log(getCurrentUser(), "created project", "PROJECT", project.getId(), project.getName());

        publisher.publishEvent(new ProjectAssignedEvent(manager.getEmail(), project.getName()));
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Override
    public void updateProject(Long projectId, UpdateProjectRequest request) {
        log.info("Updating project: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User currentUser = getCurrentUser();

        if ("MANAGER".equals(currentUser.getRole().name()) &&
                !project.getManager().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized update attempt: userId={}, projectId={}", currentUser.getId(), projectId);
            throw new UnauthorizedException("Access denied");
        }

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        // only ADMIN can update due date and status
        if (request.getDueDate() != null) {
            if (!"ADMIN".equals(currentUser.getRole().name())) {
                throw new UnauthorizedException("Only admin can update project due date");
            }
            project.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            if (!"ADMIN".equals(currentUser.getRole().name())) {
                throw new UnauthorizedException("Only admin can update project status");
            }
            if (!"ACTIVE".equals(request.getStatus()) && !"ON_HOLD".equals(request.getStatus())) {
                throw new BadRequestException("Invalid status. Allowed: ACTIVE, ON_HOLD");
            }
            project.setStatus(request.getStatus());
        }

        projectRepository.save(project);
        log.info("Project updated: id={}, name={}", project.getId(), project.getName());
        activityLogService.log(getCurrentUser(), "updated project", "PROJECT", project.getId(), project.getName());

        publisher.publishEvent(new ProjectUpdatedEvent(project.getManager().getEmail(), project.getName()));
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Override
    public void deleteProject(Long projectId) {
        log.info("Deleting project: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole().name())) {
            log.warn("Unauthorized delete attempt: userId={}, projectId={}", currentUser.getId(), projectId);
            throw new UnauthorizedException("Access denied");
        }

        String managerEmail = project.getManager() != null ? project.getManager().getEmail() : null;
        String projectName = project.getName();

        projectRepository.delete(project);
        log.info("Project deleted: id={}, name={}", projectId, projectName);
        activityLogService.log(getCurrentUser(), "deleted project", "PROJECT", projectId, projectName);

        if (managerEmail != null) {
            publisher.publishEvent(new ProjectDeletedEvent(managerEmail, projectName));
        }
    }

    @Override
    public ProjectListResponse getAllProjects() {
        log.debug("Fetching all projects");
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        List<ProjectResponse> list = projectRepository.findAllDistinct().stream().map(this::mapToResponse).toList();
        log.debug("Fetched {} projects", list.size());
        return new ProjectListResponse(list);
    }

    @Override
    public ProjectListResponse getProjectsForManager() {
        log.debug("Fetching projects for manager");
        User currentUser = getCurrentUser();
        if (!"MANAGER".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        List<ProjectResponse> list = projectRepository.findByManagerId(currentUser.getId())
                .stream().map(this::mapToResponse).toList();
        log.debug("Fetched {} projects for managerId={}", list.size(), currentUser.getId());
        return new ProjectListResponse(list);
    }

    @Override
    public ProjectListResponse getProjectsByManager(Long managerId) {
        log.debug("Fetching projects by managerId={} (cache miss)", managerId);
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        userRepository.findById(managerId).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        List<ProjectResponse> list = projectRepository.findByManagerId(managerId).stream().map(this::mapToResponse).toList();
        log.debug("Fetched {} projects for managerId={}", list.size(), managerId);
        return new ProjectListResponse(list);
    }

    @Cacheable(value = "projects",
            key = "#page + '-' + #size + '-' + #managerId + '-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    @Override
    public PaginationResponse<ProjectResponse> getAllProjects(int page, int size, Long managerId) {
        log.debug("Fetching paginated projects: page={}, size={}, managerId={} (cache miss)", page, size, managerId);
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole().name())) throw new UnauthorizedException("Access denied");

        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = managerId != null
                ? projectRepository.findByManagerId(managerId, pageable)
                : projectRepository.findAll(pageable);

        List<ProjectResponse> projectList = projectPage.getContent().stream().map(this::mapToResponse).toList();
        log.debug("Fetched {} projects (total={})", projectList.size(), projectPage.getTotalElements());

        return new PaginationResponse<>(projectList, projectPage.getNumber(), projectPage.getSize(),
                projectPage.getTotalElements(), projectPage.getTotalPages());
    }
}
