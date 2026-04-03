package com.example.backend.listener;

import com.example.backend.event.*;
import com.example.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    @Autowired
    private EmailService emailService;

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getEmail(),
                "New Task Assigned: " + event.getTitle(),
                "You have been assigned a new task '" + event.getTitle() +
                "' in project '" + event.getProjectName() + "'."
        );
    }

    @Async
    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getEmail(),
                "Task Status Updated: " + event.getTitle(),
                "Task '" + event.getTitle() + "' status has been updated to " + event.getStatus() + "."
        );
    }

    @Async
    @EventListener
    public void handleTaskDeleted(TaskDeletedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getAssignedEmail(),
                "Task Deleted: " + event.getTaskTitle(),
                "The task '" + event.getTaskTitle() + "' assigned to you has been deleted."
        );
    }

    @Async
    @EventListener
    public void handleProjectAssigned(ProjectAssignedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getManagerEmail(),
                "New Project Assigned: " + event.getProjectName(),
                "You have been assigned as manager for project '" + event.getProjectName() + "'."
        );
    }

    @Async
    @EventListener
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getManagerEmail(),
                "Project Updated: " + event.getProjectName(),
                "Project '" + event.getProjectName() + "' has been updated."
        );
    }

    @Async
    @EventListener
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        emailService.sendTaskUpdateEmail(
                event.getManagerEmail(),
                "Project Deleted: " + event.getProjectName(),
                "Project '" + event.getProjectName() + "' has been deleted by the admin."
        );
    }
}
