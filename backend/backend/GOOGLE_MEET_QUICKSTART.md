# Google Meet Integration - Quick Start

## What This Does

When creating a calendar event with attendees, the system can automatically:
1. Create a Google Meet video conference link
2. Send the link to all attendees via email
3. Add the event to Google Calendar

## Quick Setup (5 minutes)

### 1. Enable Google Calendar API

```bash
# Go to: https://console.cloud.google.com/apis/library/calendar-json.googleapis.com
# Click "Enable"
```

### 2. Create Service Account

```bash
# Go to: https://console.cloud.google.com/iam-admin/serviceaccounts
# Click "Create Service Account"
# Name: taskman-calendar
# Role: Editor
# Click "Done"
```

### 3. Download Credentials

```bash
# Click on the service account you just created
# Go to "Keys" tab
# Click "Add Key" > "Create new key" > "JSON"
# Save the file as: google-credentials.json
```

### 4. Configure Application

Add to your `.env` file or environment variables:

```bash
GOOGLE_CALENDAR_ENABLED=true
GOOGLE_CALENDAR_CREDENTIALS_FILE=/path/to/google-credentials.json
```

Or update `application.properties`:

```properties
google.calendar.enabled=true
google.calendar.credentials.file=/path/to/google-credentials.json
```

### 5. Grant Calendar Access

```bash
# 1. Copy your service account email from the credentials JSON file
#    (looks like: taskman-calendar@project-id.iam.gserviceaccount.com)
#
# 2. Go to: https://calendar.google.com
#
# 3. Click Settings (gear icon) > Settings
#
# 4. Under "Settings for my calendars", select your calendar
#
# 5. Scroll to "Share with specific people"
#
# 6. Click "Add people"
#
# 7. Paste the service account email
#
# 8. Set permission to "Make changes to events"
#
# 9. Click "Send"
```

### 6. Restart Application

```bash
# Restart your Spring Boot application
# Check logs for: "Google Calendar integration is enabled"
```

## Testing

1. Create a new calendar event in the UI
2. Check "Auto-generate Google Meet link" (enabled by default)
3. Add at least one attendee
4. Click "Create Event"
5. Check the email - it should contain a Google Meet link like:
   ```
   🎥 JOIN MEETING NOW:
   https://meet.google.com/xxx-xxxx-xxx
   ```

## Troubleshooting

### "Google Calendar integration is disabled"
- Set `GOOGLE_CALENDAR_ENABLED=true`
- Restart the application

### "Failed to create Google Meet link"
- Check that the credentials file path is correct
- Verify the service account has calendar access
- Check application logs for detailed errors

### Still not working?
See the full setup guide: `GOOGLE_MEET_SETUP.md`

## Disabling Google Meet

To disable auto-generation:

```properties
google.calendar.enabled=false
```

Or in the UI, uncheck "Auto-generate Google Meet link" and provide your own meeting link (Zoom, Teams, etc.)

## Security Note

**NEVER commit `google-credentials.json` to version control!**

Add to `.gitignore`:
```
google-credentials.json
**/google-credentials.json
```
