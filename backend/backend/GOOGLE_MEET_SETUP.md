# Google Meet Integration Setup Guide

This guide explains how to set up Google Calendar API integration to automatically generate Google Meet links for calendar events.

## Prerequisites

- Google Cloud Platform account
- Project with Google Calendar API enabled
- Service account with appropriate permissions

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your Project ID

## Step 2: Enable Google Calendar API

1. In the Google Cloud Console, go to **APIs & Services** > **Library**
2. Search for "Google Calendar API"
3. Click on it and press **Enable**

## Step 3: Create Service Account

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **Service Account**
3. Fill in the service account details:
   - Name: `taskman-calendar-service`
   - Description: `Service account for TaskMan calendar integration`
4. Click **Create and Continue**
5. Grant the service account the **Editor** role
6. Click **Done**

## Step 4: Create Service Account Key

1. In the **Credentials** page, find your service account
2. Click on the service account email
3. Go to the **Keys** tab
4. Click **Add Key** > **Create new key**
5. Select **JSON** format
6. Click **Create**
7. The JSON key file will be downloaded automatically
8. **IMPORTANT**: Keep this file secure and never commit it to version control

## Step 5: Configure Application

1. Place the downloaded JSON key file in a secure location on your server
   - Recommended: `/etc/taskman/google-credentials.json`
   - Or use environment-specific path

2. Add the following to your `application.properties` or `application.yml`:

```properties
# Enable Google Calendar integration
google.calendar.enabled=true

# Path to service account credentials JSON file
google.calendar.credentials.file=/path/to/your/google-credentials.json
```

Or in `application.yml`:

```yaml
google:
  calendar:
    enabled: true
    credentials:
      file: /path/to/your/google-credentials.json
```

3. For development, you can use environment variables:

```bash
export GOOGLE_CALENDAR_ENABLED=true
export GOOGLE_CALENDAR_CREDENTIALS_FILE=/path/to/google-credentials.json
```

## Step 6: Grant Calendar Access

The service account needs access to create events in a calendar:

### Option A: Use Service Account Email (Recommended for Production)

1. Copy the service account email (looks like `taskman-calendar-service@project-id.iam.gserviceaccount.com`)
2. Go to [Google Calendar](https://calendar.google.com)
3. Create a new calendar or use an existing one
4. Go to calendar settings > **Share with specific people**
5. Add the service account email with **Make changes to events** permission

### Option B: Domain-Wide Delegation (For G Suite/Workspace)

If you're using Google Workspace:

1. Go to **APIs & Services** > **Credentials**
2. Click on your service account
3. Check **Enable G Suite Domain-wide Delegation**
4. Note the Client ID
5. In Google Workspace Admin Console:
   - Go to **Security** > **API Controls** > **Domain-wide Delegation**
   - Add the Client ID with scope: `https://www.googleapis.com/auth/calendar`

## Step 7: Test the Integration

1. Restart your Spring Boot application
2. Create a calendar event with attendees
3. Check the logs for:
   ```
   Google Meet link created: https://meet.google.com/xxx-xxxx-xxx
   ```
4. Verify that attendees receive the email with the Google Meet link

## Troubleshooting

### "Google Calendar integration is disabled"
- Check that `google.calendar.enabled=true` in your configuration
- Restart the application after changing configuration

### "Google Calendar credentials file not configured"
- Verify the path to the credentials file is correct
- Check file permissions (application must be able to read it)

### "Failed to create Google Meet link"
- Check application logs for detailed error messages
- Verify the service account has calendar access
- Ensure Google Calendar API is enabled in your project
- Check that the credentials file is valid JSON

### "No Google Meet link found in created event"
- Verify that `conferenceDataVersion=1` is set in the API call
- Check that the service account has permission to create conference data
- Ensure the calendar supports Google Meet (some calendars may not)

## Security Best Practices

1. **Never commit credentials to version control**
   - Add `google-credentials.json` to `.gitignore`
   - Use environment variables or secure secret management

2. **Restrict file permissions**
   ```bash
   chmod 600 /path/to/google-credentials.json
   ```

3. **Use separate service accounts for different environments**
   - Development, staging, and production should have separate accounts

4. **Regularly rotate service account keys**
   - Create new keys periodically
   - Delete old keys after rotation

5. **Monitor API usage**
   - Check Google Cloud Console for unusual activity
   - Set up billing alerts

## Fallback Behavior

If Google Meet integration fails or is disabled:
- The application will continue to work normally
- Users can manually provide meeting links (Zoom, Teams, etc.)
- No error will be shown to end users
- Errors are logged for administrators

## API Quotas

Google Calendar API has the following quotas (as of 2024):
- 1,000,000 queries per day
- 10 queries per second per user

For most applications, these limits are sufficient. Monitor your usage in the Google Cloud Console.
