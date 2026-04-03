package com.example.backend.event;

public class TaskAssignedEvent {

    private final String email;
    private final String title;
    private final String projectName;

    public TaskAssignedEvent(String email, String title, String projectName) {
        this.email = email;
        this.title = title;
        this.projectName = projectName;
    }

    public String getEmail() { return email; }
    public String getTitle() { return title; }
    public String getProjectName() { return projectName; }
}
