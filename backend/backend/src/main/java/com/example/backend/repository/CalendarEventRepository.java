package com.example.backend.repository;

import com.example.backend.Entities.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // Events created by user OR user is an attendee
    @Query("""
        SELECT DISTINCT e FROM CalendarEvent e
        LEFT JOIN FETCH e.attendees
        LEFT JOIN FETCH e.createdBy
        WHERE e.createdBy.id = :userId
           OR :userId IN (SELECT a.id FROM e.attendees a)
        ORDER BY e.startTime ASC
    """)
    List<CalendarEvent> findAllForUser(@Param("userId") Long userId);

    // Events in a date range for a user
    @Query("""
        SELECT DISTINCT e FROM CalendarEvent e
        LEFT JOIN FETCH e.attendees
        LEFT JOIN FETCH e.createdBy
        WHERE (e.createdBy.id = :userId OR :userId IN (SELECT a.id FROM e.attendees a))
          AND e.startTime >= :from
          AND e.startTime <= :to
        ORDER BY e.startTime ASC
    """)
    List<CalendarEvent> findForUserInRange(
        @Param("userId") Long userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
