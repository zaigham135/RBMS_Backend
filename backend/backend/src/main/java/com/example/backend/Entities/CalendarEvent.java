package com.example.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location;

    // Meeting link (Zoom, Google Meet, etc.)
    @Column(name = "meeting_link")
    private String meetingLink;

    // Color label for UI
    private String color;

    // Google Calendar event ID (if synced)
    @Column(name = "google_event_id")
    private String googleEventId;

    // Creator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Attendees (users who should see this event)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "calendar_event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> attendees = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}
