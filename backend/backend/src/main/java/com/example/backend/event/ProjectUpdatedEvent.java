package com.example.backend.event;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fired whenever a project is updated.
 * {@code changes} holds only the fields that actually changed.
 */
public class ProjectUpdatedEvent {

    private final String managerEmail;
    private final String projectName;
    private final String updatedBy;
    private final Map<String, String> changes;

    public ProjectUpdatedEvent(String managerEmail, String projectName,
                               String updatedBy, Map<String, String> changes) {
        this.managerEmail = managerEmail;
        this.projectName  = projectName;
        this.updatedBy    = updatedBy;
        this.changes      = changes;
    }

    public String getManagerEmail()          { return managerEmail; }
    public String getProjectName()           { return projectName; }
    public String getUpdatedBy()             { return updatedBy; }
    public Map<String, String> getChanges()  { return changes; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder(String managerEmail, String projectName, String updatedBy) {
        return new Builder(managerEmail, projectName, updatedBy);
    }

    public static class Builder {
        private final String managerEmail, projectName, updatedBy;
        private final Map<String, String> changes = new LinkedHashMap<>();

        Builder(String managerEmail, String projectName, String updatedBy) {
            this.managerEmail = managerEmail;
            this.projectName  = projectName;
            this.updatedBy    = updatedBy;
        }

        public Builder name(String oldName, String newName) {
            if (newName != null && !newName.isBlank() && !newName.equals(oldName))
                changes.put("Name", oldName + " → " + newName);
            return this;
        }
        public Builder description(String v) {
            if (v != null && !v.isBlank()) changes.put("Description", truncate(v, 120));
            return this;
        }
        public Builder dueDate(LocalDate v) {
            if (v != null) changes.put("Due Date", v.toString());
            return this;
        }
        public Builder status(String v) {
            if (v != null && !v.isBlank()) changes.put("Status", v);
            return this;
        }

        public ProjectUpdatedEvent build() {
            return new ProjectUpdatedEvent(managerEmail, projectName, updatedBy, changes);
        }

        private static String truncate(String s, int max) {
            return s.length() <= max ? s : s.substring(0, max) + "…";
        }
    }
}
