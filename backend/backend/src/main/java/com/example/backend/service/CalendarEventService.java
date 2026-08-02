package com.example.backend.service;

import com.example.backend.Entities.CalendarEvent;
import com.example.backend.Entities.User;
import com.example.backend.dto.request.CreateCalendarEventRequest;
import com.example.backend.dto.response.CalendarEventResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CalendarEventRepository;
import com.example.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    private static final Logger log = LoggerFactory.getLogger(CalendarEventService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired private CalendarEventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private GoogleMeetService googleMeetService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CalendarEventResponse toResponse(CalendarEvent e) {
        List<CalendarEventResponse.AttendeeInfo> attendees = e.getAttendees().stream()
                .map(a -> new CalendarEventResponse.AttendeeInfo(
                        a.getId(), a.getName(), a.getEmail(), a.getProfilePhoto()))
                .collect(Collectors.toList());

        return new CalendarEventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStartTime() != null ? e.getStartTime().format(FMT) : null,
                e.getEndTime() != null ? e.getEndTime().format(FMT) : null,
                e.getLocation(),
                e.getMeetingLink(),
                e.getColor(),
                e.getGoogleEventId(),
                e.getCreatedBy() != null ? e.getCreatedBy().getName() : null,
                e.getCreatedBy() != null ? e.getCreatedBy().getEmail() : null,
                attendees
        );
    }

    public CalendarEventResponse createEvent(CreateCalendarEventRequest req) {
        User creator = getCurrentUser();

        CalendarEvent event = new CalendarEvent();
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setStartTime(LocalDateTime.parse(req.getStartTime(), FMT));
        event.setEndTime(LocalDateTime.parse(req.getEndTime(), FMT));
        event.setLocation(req.getLocation());
        event.setColor(req.getColor() != null ? req.getColor() : "blue");
        event.setCreatedBy(creator);

        // Resolve attendees
        List<User> attendees = new ArrayList<>();
        if (req.getAttendeeIds() != null) {
            for (Long uid : req.getAttendeeIds()) {
                userRepository.findById(uid).ifPresent(attendees::add);
            }
        }
        event.setAttendees(attendees);

        // Auto-generate Google Meet link if requested or if no meeting link provided
        String meetingLink = req.getMeetingLink();
        if ((req.isAutoGenerateGoogleMeet() || (meetingLink == null || meetingLink.isBlank())) && !attendees.isEmpty()) {
            List<String> attendeeEmails = attendees.stream()
                    .map(User::getEmail)
                    .collect(java.util.stream.Collectors.toList());
            
            String googleMeetLink = googleMeetService.createGoogleMeetLink(
                    event.getTitle(),
                    event.getDescription(),
                    event.getStartTime(),
                    event.getEndTime(),
                    attendeeEmails
            );
            
            if (googleMeetLink != null) {
                meetingLink = googleMeetLink;
                log.info("Auto-generated Google Meet link for event: {}", event.getTitle());
            }
        }
        
        event.setMeetingLink(meetingLink);

        CalendarEvent saved = eventRepository.save(event);
        log.info("Calendar event created: id={}, title={}, by={}", saved.getId(), saved.getTitle(), creator.getEmail());

        // Send email notifications async
        sendEventInvitations(saved, creator, attendees);

        return toResponse(saved);
    }

    @Async
    protected void sendEventInvitations(CalendarEvent event, User creator, List<User> attendees) {
        String subject = "Event Invitation: " + event.getTitle();
        String html = buildInviteEmail(event, creator);

        for (User attendee : attendees) {
            if (!attendee.getId().equals(creator.getId())) {
                emailService.sendTaskUpdateEmail(attendee.getEmail(), subject, html);
                log.info("Event invite sent to: {}", attendee.getEmail());
            }
        }
    }

    private String buildInviteEmail(CalendarEvent event, User creator) {
        String startFmt = event.getStartTime() != null
                ? event.getStartTime().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy  h:mm a"))
                : "TBD";
        String endFmt = event.getEndTime() != null
                ? event.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a"))
                : "TBD";

        // Build Google Calendar add-link
        String gcalStart = event.getStartTime() != null
                ? event.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) : "";
        String gcalEnd = event.getEndTime() != null
                ? event.getEndTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) : "";
        String gcalLink = "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + encode(event.getTitle())
                + "&dates=" + gcalStart + "/" + gcalEnd
                + "&details=" + encode(event.getDescription() != null ? event.getDescription() : "")
                + "&location=" + encode(event.getLocation() != null ? event.getLocation() : "");

        // Info box rows
        java.util.List<String> kvList = new java.util.ArrayList<>();
        kvList.add("Event");     kvList.add("<strong>" + event.getTitle() + "</strong>");
        kvList.add("Organizer"); kvList.add(creator.getName() + " &lt;" + creator.getEmail() + "&gt;");
        kvList.add("Date &amp; Time"); kvList.add(startFmt + " – " + endFmt);
        if (event.getLocation() != null && !event.getLocation().isBlank())
            { kvList.add("Location"); kvList.add(event.getLocation()); }
        if (event.getDescription() != null && !event.getDescription().isBlank())
            { kvList.add("Description"); kvList.add(truncate(event.getDescription(), 200)); }

        String infoBox = emailService.buildInfoBox(kvList.toArray(new String[0]));

        // Meeting link button (if present)
        String meetingBlock = "";
        if (event.getMeetingLink() != null && !event.getMeetingLink().isBlank()) {
            meetingBlock = "<div style='text-align:center;margin:20px 0 8px;'>"
                + "<a href='" + event.getMeetingLink() + "' target='_blank' "
                + "style='display:inline-block;background:#4c8cff;color:#ffffff;font-size:15px;"
                + "font-weight:700;padding:14px 32px;border-radius:12px;text-decoration:none;"
                + "letter-spacing:-0.2px;box-shadow:0 6px 20px rgba(76,140,255,0.4);'>"
                + "&#127909; Join Meeting"
                + "</a></div>";
        }

        // Add to Google Calendar link
        String gcalBlock = "<div style='text-align:center;margin:12px 0 0;'>"
                + "<a href='" + gcalLink + "' target='_blank' "
                + "style='display:inline-block;border:1px solid #334155;color:#94a3b8;font-size:13px;"
                + "font-weight:600;padding:10px 24px;border-radius:10px;text-decoration:none;'>"
                + "&#128197; Add to Google Calendar"
                + "</a></div>";

        String content = infoBox + meetingBlock + gcalBlock;

        return emailService.buildHtml(
            "You&apos;re invited to an event",
            creator.getName() + " has invited you to the following event on TaskMan.",
            content,
            "This event has been added to your TaskMan calendar. Log in to view all your upcoming events and meetings."
        );
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    public List<CalendarEventResponse> getMyEvents() {
        User user = getCurrentUser();
        return eventRepository.findAllForUser(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CalendarEventResponse> getMyEventsInRange(String from, String to) {
        User user = getCurrentUser();
        LocalDateTime fromDt = LocalDateTime.parse(from, FMT);
        LocalDateTime toDt = LocalDateTime.parse(to, FMT);
        return eventRepository.findForUserInRange(user.getId(), fromDt, toDt)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void deleteEvent(Long eventId) {
        User user = getCurrentUser();
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!event.getCreatedBy().getId().equals(user.getId())) {
            throw new com.example.backend.exception.UnauthorizedException("You can only delete your own events");
        }
        eventRepository.delete(event);
        log.info("Calendar event deleted: id={}", eventId);
    }
}
