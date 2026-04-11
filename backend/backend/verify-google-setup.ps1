# Google Meet Integration Setup Verification Script
# Run this script to verify your Google Calendar integration is configured correctly

Write-Host "`n=== Google Meet Integration Setup Verification ===" -ForegroundColor Cyan
Write-Host ""

$allGood = $true

# Check 1: Credentials file exists
Write-Host "1. Checking credentials file..." -NoNewline
if (Test-Path "config/google-credentials.json") {
    Write-Host " ✅ FOUND" -ForegroundColor Green
    
    # Check if it's valid JSON
    try {
        $json = Get-Content "config/google-credentials.json" -Raw | ConvertFrom-Json
        if ($json.type -eq "service_account") {
            Write-Host "   Service Account Email: $($json.client_email)" -ForegroundColor Gray
            Write-Host "   Project ID: $($json.project_id)" -ForegroundColor Gray
        } else {
            Write-Host "   ⚠️  Warning: Not a service account credential" -ForegroundColor Yellow
            $allGood = $false
        }
    } catch {
        Write-Host "   ❌ Invalid JSON format" -ForegroundColor Red
        $allGood = $false
    }
} else {
    Write-Host " ❌ NOT FOUND" -ForegroundColor Red
    Write-Host "   Expected location: config/google-credentials.json" -ForegroundColor Gray
    $allGood = $false
}

# Check 2: .gitignore includes credentials
Write-Host "`n2. Checking .gitignore..." -NoNewline
if (Test-Path ".gitignore") {
    $gitignore = Get-Content ".gitignore" -Raw
    if ($gitignore -match "google-credentials\.json") {
        Write-Host " ✅ PROTECTED" -ForegroundColor Green
    } else {
        Write-Host " ⚠️  NOT IN .GITIGNORE" -ForegroundColor Yellow
        Write-Host "   Warning: Credentials file should be in .gitignore" -ForegroundColor Gray
    }
} else {
    Write-Host " ⚠️  .gitignore not found" -ForegroundColor Yellow
}

# Check 3: application.properties configuration
Write-Host "`n3. Checking application.properties..." -NoNewline
if (Test-Path "src/main/resources/application.properties") {
    $props = Get-Content "src/main/resources/application.properties" -Raw
    
    $hasEnabled = $props -match "google\.calendar\.enabled"
    $hasCredFile = $props -match "google\.calendar\.credentials\.file"
    
    if ($hasEnabled -and $hasCredFile) {
        Write-Host " ✅ CONFIGURED" -ForegroundColor Green
        
        # Extract values
        if ($props -match "google\.calendar\.enabled=\$\{[^:]+:([^}]+)\}") {
            $enabledDefault = $matches[1]
            Write-Host "   Enabled (default): $enabledDefault" -ForegroundColor Gray
        }
        if ($props -match "google\.calendar\.credentials\.file=\$\{[^:]+:([^}]+)\}") {
            $fileDefault = $matches[1]
            Write-Host "   Credentials path (default): $fileDefault" -ForegroundColor Gray
        }
    } else {
        Write-Host " ❌ NOT CONFIGURED" -ForegroundColor Red
        if (-not $hasEnabled) {
            Write-Host "   Missing: google.calendar.enabled" -ForegroundColor Gray
        }
        if (-not $hasCredFile) {
            Write-Host "   Missing: google.calendar.credentials.file" -ForegroundColor Gray
        }
        $allGood = $false
    }
} else {
    Write-Host " ❌ NOT FOUND" -ForegroundColor Red
    $allGood = $false
}

# Check 4: Maven dependencies
Write-Host "`n4. Checking Maven dependencies..." -NoNewline
if (Test-Path "pom.xml") {
    $pom = Get-Content "pom.xml" -Raw
    
    $hasGoogleApi = $pom -match "google-api-client"
    $hasCalendar = $pom -match "google-api-services-calendar"
    $hasAuth = $pom -match "google-auth-library-oauth2-http"
    
    if ($hasGoogleApi -and $hasCalendar -and $hasAuth) {
        Write-Host " ✅ ALL PRESENT" -ForegroundColor Green
    } else {
        Write-Host " ❌ MISSING DEPENDENCIES" -ForegroundColor Red
        if (-not $hasGoogleApi) { Write-Host "   Missing: google-api-client" -ForegroundColor Gray }
        if (-not $hasCalendar) { Write-Host "   Missing: google-api-services-calendar" -ForegroundColor Gray }
        if (-not $hasAuth) { Write-Host "   Missing: google-auth-library-oauth2-http" -ForegroundColor Gray }
        $allGood = $false
    }
} else {
    Write-Host " ❌ pom.xml NOT FOUND" -ForegroundColor Red
    $allGood = $false
}

# Summary
Write-Host "`n" + ("=" * 50) -ForegroundColor Cyan
if ($allGood) {
    Write-Host "✅ Setup verification PASSED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Grant calendar access to service account" -ForegroundColor White
    Write-Host "   See SETUP_COMPLETE.md for instructions" -ForegroundColor Gray
    Write-Host "2. Restart your application" -ForegroundColor White
    Write-Host "3. Test by creating a calendar event" -ForegroundColor White
} else {
    Write-Host "❌ Setup verification FAILED!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please fix the issues above and run this script again." -ForegroundColor Yellow
    Write-Host "See SETUP_COMPLETE.md for detailed setup instructions." -ForegroundColor Gray
}
Write-Host ""
