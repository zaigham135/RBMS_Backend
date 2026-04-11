package com.example.backend.listener;

import com.example.backend.event.*;
import com.example.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    @Autowired
    private EmailService emailService;

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Event: TaskAssigned - to={}, task={}, project={}", event.getEmail(), event.getTitle(), event.getProjectName());
        emailService.sendTaskUpdateEmail(event.getEmail(),
                "New Task Assigned: " + event.getTitle(),
                "You have been assigned a new task '" + event.getTitle() + "' in project '" + event.getProjectName() + "'.");
    }

    @Async
    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.info("Event: TaskUpdated - to={}, task={}, status={}", event.getEmail(), event.getTitle(), event.getStatus());
        emailService.sendTaskUpdateEmail(event.getEmail(),
                "Task Status Updated: " + event.getTitle(),
                "Task '" + event.getTitle() + "' status has been updated to " + event.getStatus() + ".");
    }

    @Async
    @EventListener
    public void handleTaskDeleted(TaskDeletedEvent event) {
        log.info("Event: TaskDeleted - to={}, task={}", event.getAssignedEmail(), event.getTaskTitle());
        emailService.sendTaskUpdateEmail(event.getAssignedEmail(),
                "Task Deleted: " + event.getTaskTitle(),
                "The task '" + event.getTaskTitle() + "' assigned to you has been deleted.");
    }

    @Async
    @EventListener
    public void handleProjectAssigned(ProjectAssignedEvent event) {
        log.info("Event: ProjectAssigned - to={}, project={}", event.getManagerEmail(), event.getProjectName());
        emailService.sendTaskUpdateEmail(event.getManagerEmail(),
                "New Project Assigned: " + event.getProjectName(),
                "You have been assigned as manager for project '" + event.getProjectName() + "'.");
    }

    @Async
    @EventListener
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        log.info("Event: ProjectUpdated - to={}, project={}", event.getManagerEmail(), event.getProjectName());
        emailService.sendTaskUpdateEmail(event.getManagerEmail(),
                "Project Updated: " + event.getProjectName(),
                "Project '" + event.getProjectName() + "' has been updated.");
    }

    @Async
    @EventListener
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        log.info("Event: ProjectDeleted - to={}, project={}", event.getManagerEmail(), event.getProjectName());
        emailService.sendTaskUpdateEmail(event.getManagerEmail(),
                "Project Deleted: " + event.getProjectName(),
                "Project '" + event.getProjectName() + "' has been deleted by the admin.");
    }
}
