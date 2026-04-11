package com.example.backend.service;

import com.example.backend.Entities.ActivityLog;
import com.example.backend.Entities.User;
import com.example.backend.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Async
    public void log(User user, String action, String entityType, Long entityId, String entityName) {
        log(user, action, entityType, entityId, entityName, null, null);
    }

    @Async
    public void log(User user, String action, String entityType, Long entityId, String entityName, Long projectId, String projectName) {
        try {
            ActivityLog entry = new ActivityLog();
            entry.setUser(user);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setEntityName(entityName);
            entry.setProjectId(projectId);
            entry.setProjectName(projectName);
            activityLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    public List<ActivityLog> getRecent(int limit) {
        return activityLogRepository.findRecentWithUser(PageRequest.of(0, limit));
    }

    public List<ActivityLog> getRecentForProjects(List<Long> projectIds, int limit) {
        if (projectIds == null || projectIds.isEmpty()) return List.of();
        return activityLogRepository.findRecentByProjectIds(projectIds, PageRequest.of(0, limit));
    }

    public void deleteById(Long id) {
        if (!activityLogRepository.existsById(id)) {
            throw new com.example.backend.exception.ResourceNotFoundException("Activity log not found with id: " + id);
        }
        activityLogRepository.deleteById(id);
    }
}
