package com.example.backend.event;

public class ProjectAssignedEvent {
    private final String managerEmail;
    private final String projectName;

    public ProjectAssignedEvent(String managerEmail, String projectName) {
        this.managerEmail = managerEmail;
        this.projectName = projectName;
    }

    public String getManagerEmail() { return managerEmail; }
    public String getProjectName() { return projectName; }
}
