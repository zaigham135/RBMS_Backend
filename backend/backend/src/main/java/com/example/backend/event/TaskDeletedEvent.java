package com.example.backend.event;

public class TaskDeletedEvent {
    private final String assignedEmail;
    private final String taskTitle;

    public TaskDeletedEvent(String assignedEmail, String taskTitle) {
        this.assignedEmail = assignedEmail;
        this.taskTitle = taskTitle;
    }

    public String getAssignedEmail() { return assignedEmail; }
    public String getTaskTitle() { return taskTitle; }
}
