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

    // ── Task events ───────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Event: TaskAssigned - to={}, task={}, project={}", event.getEmail(), event.getTitle(), event.getProjectName());

        String content = emailService.buildInfoBox(
            "Task",    "<strong>" + event.getTitle() + "</strong>",
            "Project", event.getProjectName(),
            "Status",  emailService.buildBadge("Assigned", "#4c8cff")
        );

        String html = emailService.buildHtml(
            "You have a new task",
            "A new task has been assigned to you. Please review the details below and get started.",
            content,
            "Log in to your TaskMan dashboard to view full task details, update your progress, and collaborate with your team."
        );

        emailService.sendTaskUpdateEmail(event.getEmail(), "New Task Assigned: " + event.getTitle(), html);
    }

    @Async
    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.info("Event: TaskUpdated - to={}, task={}, changes={}", event.getEmail(), event.getTitle(), event.getChanges());

        // Build info box rows: Task, Project, Updated By, then each changed field
        java.util.List<String> kvList = new java.util.ArrayList<>();
        kvList.add("Task");    kvList.add("<strong>" + event.getTitle() + "</strong>");
        kvList.add("Project"); kvList.add(event.getProjectName() != null ? event.getProjectName() : "—");
        kvList.add("Updated By"); kvList.add(event.getUpdatedBy() != null ? event.getUpdatedBy() : "—");

        for (java.util.Map.Entry<String, String> entry : event.getChanges().entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            // Wrap status and priority in colored badges
            if ("Status".equals(label)) {
                value = emailService.buildBadge(value, statusColor(value));
            } else if ("Priority".equals(label)) {
                value = emailService.buildBadge(value, priorityColor(value));
            }
            kvList.add(label);
            kvList.add(value);
        }

        String content = emailService.buildInfoBox(kvList.toArray(new String[0]));

        String html = emailService.buildHtml(
            "Task updated",
            "The following changes were made to a task assigned to you or your team.",
            content,
            "Log in to your TaskMan dashboard to review the full task details and take any required action."
        );

        emailService.sendTaskUpdateEmail(event.getEmail(), "Task Updated: " + event.getTitle(), html);
    }

    @Async
    @EventListener
    public void handleTaskDeleted(TaskDeletedEvent event) {
        log.info("Event: TaskDeleted - to={}, task={}", event.getAssignedEmail(), event.getTaskTitle());

        String content = emailService.buildInfoBox(
            "Task",   "<strong>" + event.getTaskTitle() + "</strong>",
            "Action", emailService.buildBadge("Removed", "#f472b6")
        );

        String html = emailService.buildHtml(
            "A task has been removed",
            "A task that was previously assigned to you has been deleted by an administrator.",
            content,
            "If you believe this was done in error, please contact your manager or system administrator."
        );

        emailService.sendTaskUpdateEmail(event.getAssignedEmail(), "Task Removed: " + event.getTaskTitle(), html);
    }

    // ── Project events ────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void handleProjectAssigned(ProjectAssignedEvent event) {
        log.info("Event: ProjectAssigned - to={}, project={}", event.getManagerEmail(), event.getProjectName());

        String content = emailService.buildInfoBox(
            "Project", "<strong>" + event.getProjectName() + "</strong>",
            "Role",    emailService.buildBadge("Project Manager", "#7c66ff")
        );

        String html = emailService.buildHtml(
            "You have been assigned to a project",
            "You have been designated as the Project Manager for a new project. Please review the details below.",
            content,
            "Log in to your TaskMan dashboard to view the project scope, manage your team, and track progress."
        );

        emailService.sendTaskUpdateEmail(event.getManagerEmail(), "Project Assigned: " + event.getProjectName(), html);
    }

    @Async
    @EventListener
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        log.info("Event: ProjectUpdated - to={}, project={}, changes={}", event.getManagerEmail(), event.getProjectName(), event.getChanges());

        java.util.List<String> kvList = new java.util.ArrayList<>();
        kvList.add("Project");    kvList.add("<strong>" + event.getProjectName() + "</strong>");
        kvList.add("Updated By"); kvList.add(event.getUpdatedBy() != null ? event.getUpdatedBy() : "Administrator");

        for (java.util.Map.Entry<String, String> entry : event.getChanges().entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            if ("Status".equals(label)) {
                String color = "ACTIVE".equalsIgnoreCase(value) ? "#4ade80" : "#fbbf24";
                value = emailService.buildBadge(value, color);
            }
            kvList.add(label);
            kvList.add(value);
        }

        String content = emailService.buildInfoBox(kvList.toArray(new String[0]));

        String html = emailService.buildHtml(
            "Project updated",
            "A project you are managing has been updated. Please review the changes below.",
            content,
            "Log in to your TaskMan dashboard to view the full project details and ensure your team is aligned with the latest changes."
        );

        emailService.sendTaskUpdateEmail(event.getManagerEmail(), "Project Updated: " + event.getProjectName(), html);
    }

    @Async
    @EventListener
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        log.info("Event: ProjectDeleted - to={}, project={}", event.getManagerEmail(), event.getProjectName());

        String content = emailService.buildInfoBox(
            "Project", "<strong>" + event.getProjectName() + "</strong>",
            "Action",  emailService.buildBadge("Deleted", "#ef4444")
        );

        String html = emailService.buildHtml(
            "A project has been deleted",
            "A project you were managing has been permanently deleted by an administrator.",
            content,
            "If you have any concerns regarding this action, please reach out to your system administrator."
        );

        emailService.sendTaskUpdateEmail(event.getManagerEmail(), "Project Deleted: " + event.getProjectName(), html);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String statusColor(String status) {
        if (status == null) return "#94a3b8";
        return switch (status.toUpperCase()) {
            case "COMPLETED", "DONE"  -> "#4ade80";
            case "IN_PROGRESS"        -> "#4c8cff";
            case "IN_REVIEW"          -> "#a78bfa";
            case "PENDING", "TODO"    -> "#fbbf24";
            case "CANCELLED"          -> "#ef4444";
            default                   -> "#94a3b8";
        };
    }

    private String priorityColor(String priority) {
        if (priority == null) return "#94a3b8";
        return switch (priority.toUpperCase()) {
            case "HIGH"   -> "#ef4444";
            case "MEDIUM" -> "#fbbf24";
            case "LOW"    -> "#4ade80";
            default       -> "#94a3b8";
        };
    }
}
