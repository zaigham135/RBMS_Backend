package com.example.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateCalendarEventRequest {
    private String title;
    private String description;
    private String startTime;   // ISO datetime: "2026-04-15T14:00:00"
    private String endTime;     // ISO datetime: "2026-04-15T16:00:00"
    private String location;
    private String meetingLink; // Zoom, Google Meet, etc. (optional - will auto-generate if not provided)
    private String color;
    private List<Long> attendeeIds;  // user IDs to invite
    private boolean syncToGoogle;    // whether to push to Google Calendar
    private boolean autoGenerateGoogleMeet = true;  // auto-generate Google Meet link if no meetingLink provided
}
