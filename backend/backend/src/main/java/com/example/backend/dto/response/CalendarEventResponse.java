package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String location;
    private String meetingLink;
    private String color;
    private String googleEventId;
    private String createdByName;
    private String createdByEmail;
    private List<AttendeeInfo> attendees;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendeeInfo {
        private Long id;
        private String name;
        private String email;
        private String profilePhoto;
    }
}
