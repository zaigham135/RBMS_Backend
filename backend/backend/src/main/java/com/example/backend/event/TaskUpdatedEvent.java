package com.example.backend.event;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fired whenever a task is updated.
 * {@code changes} holds only the fields that actually changed,
 * keyed by a human-readable label (e.g. "Status", "Due Date").
 */
public class TaskUpdatedEvent {

    private final String email;
    private final String title;
    private final String projectName;
    private final String updatedBy;
    /** Ordered map of label → new value (only changed fields). */
    private final Map<String, String> changes;

    public TaskUpdatedEvent(String email, String title, String projectName,
                            String updatedBy, Map<String, String> changes) {
        this.email       = email;
        this.title       = title;
        this.projectName = projectName;
        this.updatedBy   = updatedBy;
        this.changes     = changes;
    }

    public String getEmail()       { return email; }
    public String getTitle()       { return title; }
    public String getProjectName() { return projectName; }
    public String getUpdatedBy()   { return updatedBy; }
    public Map<String, String> getChanges() { return changes; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder(String email, String title, String projectName, String updatedBy) {
        return new Builder(email, title, projectName, updatedBy);
    }

    public static class Builder {
        private final String email, title, projectName, updatedBy;
        private final Map<String, String> changes = new LinkedHashMap<>();

        Builder(String email, String title, String projectName, String updatedBy) {
            this.email       = email;
            this.title       = title;
            this.projectName = projectName;
            this.updatedBy   = updatedBy;
        }

        public Builder status(String v)      { if (v != null) changes.put("Status", v); return this; }
        public Builder priority(String v)    { if (v != null) changes.put("Priority", v); return this; }
        public Builder dueDate(LocalDate v)  { if (v != null) changes.put("Due Date", v.toString()); return this; }
        public Builder description(String v) { if (v != null && !v.isBlank()) changes.put("Description", truncate(v, 120)); return this; }
        public Builder comment(String v)     { if (v != null && !v.isBlank()) changes.put("Comment Added", truncate(v, 120)); return this; }

        public TaskUpdatedEvent build() { return new TaskUpdatedEvent(email, title, projectName, updatedBy, changes); }

        private static String truncate(String s, int max) {
            return s.length() <= max ? s : s.substring(0, max) + "…";
        }
    }
}
