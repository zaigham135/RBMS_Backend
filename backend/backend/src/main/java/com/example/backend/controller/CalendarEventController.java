package com.example.backend.controller;

import com.example.backend.dto.request.CreateCalendarEventRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CalendarEventResponse;
import com.example.backend.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarEventController {

    @Autowired
    private CalendarEventService calendarEventService;

    /** Create a new event — sends email invitations to all attendees */
    @PostMapping("/events")
    public ApiResponse<CalendarEventResponse> createEvent(@RequestBody CreateCalendarEventRequest req) {
        return new ApiResponse<>("success", "Event created", calendarEventService.createEvent(req));
    }

    /** Get all events visible to the current user (created by them or invited) */
    @GetMapping("/events")
    public ApiResponse<List<CalendarEventResponse>> getMyEvents() {
        return new ApiResponse<>("success", "Events fetched", calendarEventService.getMyEvents());
    }

    /** Get events in a date range */
    @GetMapping("/events/range")
    public ApiResponse<List<CalendarEventResponse>> getEventsInRange(
            @RequestParam String from,
            @RequestParam String to) {
        return new ApiResponse<>("success", "Events fetched", calendarEventService.getMyEventsInRange(from, to));
    }

    /** Delete an event (creator only) */
    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return new ApiResponse<>("success", "Event deleted", null);
    }
}
