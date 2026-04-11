package com.example.backend.event;

public class ProjectUpdatedEvent {
    private final String managerEmail;
    private final String projectName;

    public ProjectUpdatedEvent(String managerEmail, String projectName) {
        this.managerEmail = managerEmail;
        this.projectName = projectName;
    }

    public String getManagerEmail() { return managerEmail; }
    public String getProjectName() { return projectName; }
}
