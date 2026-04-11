package com.example.backend.event;

public class TaskUpdatedEvent {

    private String email;
    private String title;
    private String status;

    public TaskUpdatedEvent(String email, String title, String status) {
        this.email = email;
        this.title = title;
        this.status = status;
    }

    public String getEmail() { return email; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
}
