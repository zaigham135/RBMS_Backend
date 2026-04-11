# 🎯 Final Step: Grant Calendar Access

## ✅ Setup Status: Almost Complete!

All configuration is done. You just need to grant the service account access to your Google Calendar.

## 📋 Quick Instructions

### 1. Copy the Service Account Email

```
rbms-373@weighty-wonder-396515.iam.gserviceaccount.com
```

### 2. Open Google Calendar

Go to: [https://calendar.google.com](https://calendar.google.com)

### 3. Open Calendar Settings

- Click the **⚙️ Settings** gear icon (top right)
- Click **Settings** from the dropdown

### 4. Select Your Calendar

- In the left sidebar, under "Settings for my calendars"
- Click on your calendar (usually your email address)

### 5. Share with Service Account

- Scroll down to the **"Share with specific people"** section
- Click **"+ Add people"**

### 6. Add the Service Account

- Paste the email: `rbms-373@weighty-wonder-396515.iam.gserviceaccount.com`
- Set permission to: **"Make changes to events"**
- Click **"Send"**

### 7. Restart Your Application

Stop and restart your Spring Boot backend application.

### 8. Test It! 🚀

1. Go to Employee Dashboard → Calendar
2. Click "Add Event"
3. Fill in:
   - Title: "Test Meeting"
   - Add at least one attendee
   - Keep "Auto-generate Google Meet link" checked ✅
4. Click "Create Event"
5. Check the email - should contain:
   ```
   🎥 JOIN MEETING NOW:
   https://meet.google.com/xxx-xxxx-xxx
   ```

## 🎉 That's It!

Once you complete step 5 (granting calendar access), the integration is fully functional.

## 📸 Visual Guide

### Step 3-4: Calendar Settings
```
Settings (⚙️) → Settings → [Your Calendar Name]
```

### Step 5: Share Section
Look for this section:
```
┌─────────────────────────────────────┐
│ Share with specific people          │
│                                     │
│ [+ Add people]                      │
└─────────────────────────────────────┘
```

### Step 6: Add Service Account
```
┌─────────────────────────────────────┐
│ Add people                          │
│                                     │
│ Email: rbms-373@weighty-wonder...   │
│ Permission: Make changes to events  │
│                                     │
│ [Send]                              │
└─────────────────────────────────────┘
```

## ❓ Need Help?

If something doesn't work:
1. Check application logs for error messages
2. See `SETUP_COMPLETE.md` for troubleshooting
3. Verify all steps in the checklist

## 🔒 Security Reminder

- ✅ Credentials file is protected in .gitignore
- ✅ Never commit `google-credentials.json` to Git
- ✅ Keep the credentials file secure
