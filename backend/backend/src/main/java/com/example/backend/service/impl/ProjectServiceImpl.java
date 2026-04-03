package com.example.backend.service.impl;

import com.example.backend.Entities.Project;
import com.example.backend.Entities.User;
import com.example.backend.dto.request.CreateProjectRequest;
import com.example.backend.dto.request.UpdateProjectRequest;
import com.example.backend.dto.response.PaginationResponse;
import com.example.backend.dto.response.ProjectListResponse;
import com.example.backend.dto.response.ProjectResponse;
import com.example.backend.event.ProjectAssignedEvent;
import com.example.backend.event.ProjectDeletedEvent;
import com.example.backend.event.ProjectUpdatedEvent;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ProjectService;
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

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

    private ProjectResponse mapToResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getManager() != null ? p.getManager().getId() : null,
                p.getManager() != null ? p.getManager().getName() : null,
                p.getManager() != null ? p.getManager().getEmail() : null
        );
    }

    @CacheEvict(value = "projects", allEntries = true) // ✅ clears cache on create
    @Override
    public void createProject(CreateProjectRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Project name is required");
        }

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (!"MANAGER".equals(manager.getRole().name())) {
            throw new BadRequestException("Assigned user is not a manager");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setManager(manager);

        projectRepository.save(project);

        publisher.publishEvent(new ProjectAssignedEvent(manager.getEmail(), project.getName()));
    }
    @Override
    public void updateProject(Long projectId, UpdateProjectRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User currentUser = getCurrentUser();

        if ("MANAGER".equals(currentUser.getRole().name()) &&
                !project.getManager().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        projectRepository.save(project);

        publisher.publishEvent(new ProjectUpdatedEvent(project.getManager().getEmail(), project.getName()));
    }
    @Override
    public void deleteProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Access denied");
        }

        String managerEmail = project.getManager() != null ? project.getManager().getEmail() : null;
        String projectName = project.getName();

        projectRepository.delete(project);

        if (managerEmail != null) {
            publisher.publishEvent(new ProjectDeletedEvent(managerEmail, projectName));
        }
    }
    @Cacheable(
            value = "projects",
            key = "'all-' + T(org.springframework.security.core.context.SecurityContextHolder)" +
                    ".getContext().getAuthentication().getName()"
    )
    @Override
    public ProjectListResponse getAllProjects() {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Access denied");
        }

        List<ProjectResponse> list = projectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ProjectListResponse(list);
    }

    @Cacheable(
            value = "projects",
            key = "'manager-' + T(org.springframework.security.core.context.SecurityContextHolder)" +
                    ".getContext().getAuthentication().getName()"
    )
    @Override
    public ProjectListResponse getProjectsForManager() {

        User currentUser = getCurrentUser();

        if (!"MANAGER".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Access denied");
        }

        List<ProjectResponse> list = projectRepository.findByManagerId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ProjectListResponse(list);
    }

    @Cacheable(
            value = "projects",
            key = "'by-manager-' + #managerId"
    )
    @Override
    public ProjectListResponse getProjectsByManager(Long managerId) {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Access denied");
        }

        userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        List<ProjectResponse> list = projectRepository.findByManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ProjectListResponse(list);
    }

    // ✅ fixed cache key — uses actual method params
    @Cacheable(
            value = "projects",
            key = "#page + '-' + #size + '-' + #managerId + '-' + " +
                    "T(org.springframework.security.core.context.SecurityContextHolder)" +
                    ".getContext().getAuthentication().getName()"
    )
    @Override
    public PaginationResponse<ProjectResponse> getAllProjects(int page, int size, Long managerId) {

        User currentUser = getCurrentUser();
        System.out.println("CACHE TEST - METHOD EXECUTED");
        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Access denied");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Project> projectPage = managerId != null
                ? projectRepository.findByManagerId(managerId, pageable)
                : projectRepository.findAll(pageable);

        List<ProjectResponse> projectList = projectPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PaginationResponse<>(
                projectList,
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages()
        );
    }
}