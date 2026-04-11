package com.example.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleMeetService {

    private static final Logger log = LoggerFactory.getLogger(GoogleMeetService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "TaskMan Calendar";

    @Value("${google.calendar.credentials.file:#{null}}")
    private String credentialsFilePath;

    @Value("${google.calendar.enabled:false}")
    private boolean googleCalendarEnabled;

    /**
     * Creates a Google Meet link for the given event details.
     * Returns null if Google Calendar integration is not enabled or fails.
     * Note: Attendees are not added to the Google Calendar event to avoid Domain-Wide Delegation requirement.
     * Attendees will receive the meeting link via email instead.
     */
    public String createGoogleMeetLink(String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<String> attendeeEmails) {
        if (!googleCalendarEnabled) {
            log.info("Google Calendar integration is disabled");
            return null;
        }

        if (credentialsFilePath == null || credentialsFilePath.isEmpty()) {
            log.warn("Google Calendar credentials file not configured");
            return null;
        }

        try {
            Calendar service = getCalendarService();
            // Don't pass attendees to avoid "forbiddenForServiceAccounts" error
            Event event = createCalendarEvent(title, description, startTime, endTime, null);
            
            // Insert event into primary calendar
            Event createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            // Extract Google Meet link
            if (createdEvent.getConferenceData() != null && 
                createdEvent.getConferenceData().getEntryPoints() != null) {
                
                for (EntryPoint entryPoint : createdEvent.getConferenceData().getEntryPoints()) {
                    if ("video".equals(entryPoint.getEntryPointType())) {
                        String meetLink = entryPoint.getUri();
                        log.info("Google Meet link created: {}", meetLink);
                        return meetLink;
                    }
                }
            }

            log.warn("No Google Meet link found in created event");
            return null;

        } catch (Exception e) {
            log.error("Failed to create Google Meet link", e);
            return null;
        }
    }

    private Calendar getCalendarService() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsFilePath))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Event createCalendarEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<String> attendeeEmails) {
        Event event = new Event()
                .setSummary(title)
                .setDescription(description);

        // Set start and end times
        DateTime start = new DateTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone("UTC"));

        DateTime end = new DateTime(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("UTC"));

        // Don't add attendees - service accounts can't invite attendees without Domain-Wide Delegation
        // Attendees will receive the meeting link via email instead
        
        // Request Google Meet conference
        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey()
                .setType("hangoutsMeet");
        
        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest()
                .setRequestId(java.util.UUID.randomUUID().toString())
                .setConferenceSolutionKey(conferenceSolutionKey);
        
        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(createConferenceRequest);
        
        event.setConferenceData(conferenceData);

        return event;
    }
}
