package com.example.backend.event;

public class ProjectDeletedEvent {
    private final String managerEmail;
    private final String projectName;

    public ProjectDeletedEvent(String managerEmail, String projectName) {
        this.managerEmail = managerEmail;
        this.projectName = projectName;
    }

    public String getManagerEmail() { return managerEmail; }
    public String getProjectName() { return projectName; }
}
