# Script to completely remove secrets from Git history using BFG Repo-Cleaner
# This is the nuclear option - it rewrites ALL history

Write-Host "🔒 Removing secrets from ENTIRE Git history..." -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠️  WARNING: This will rewrite Git history!" -ForegroundColor Red
Write-Host "⚠️  All collaborators will need to re-clone the repository!" -ForegroundColor Red
Write-Host ""
Write-Host "Continue? (Y/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -ne 'Y' -and $response -ne 'y') {
    Write-Host "❌ Aborted." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "📝 Step 1: Creating replacement file..." -ForegroundColor Cyan

# Create a temporary file with placeholder values
$replacementContent = @"
# ── Mail ──────────────────────────────────────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=`${MAIL_USERNAME}
spring.mail.password=`${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# ── Google OAuth2 ─────────────────────────────────────────────────────────────
spring.security.oauth2.client.registration.google.client-id=`${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=`${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# ── ImageKit ──────────────────────────────────────────────────────────────────
imagekit.public-key=`${IMAGEKIT_PUBLIC_KEY}
imagekit.private-key=`${IMAGEKIT_PRIVATE_KEY}
imagekit.url-endpoint=`${IMAGEKIT_URL_ENDPOINT}
"@

Write-Host ""
Write-Host "📝 Step 2: Using git filter-repo to rewrite history..." -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  Installing git-filter-repo if needed..." -ForegroundColor Yellow

# Check if git-filter-repo is installed
$filterRepoExists = Get-Command git-filter-repo -ErrorAction SilentlyContinue

if (-not $filterRepoExists) {
    Write-Host "Installing git-filter-repo via pip..." -ForegroundColor Cyan
    pip install git-filter-repo
}

Write-Host ""
Write-Host "📝 Step 3: Rewriting Git history (this may take a minute)..." -ForegroundColor Cyan

# Use git filter-repo to remove the secrets
git filter-repo --path backend/backend/src/main/resources/application.properties --invert-paths --force

Write-Host ""
Write-Host "📝 Step 4: Re-adding the cleaned file..." -ForegroundColor Cyan

# Re-add the file with clean content
git add backend/backend/src/main/resources/application.properties
git add .gitignore
git add backend/backend/.env.example

git commit -m "Add application.properties without secrets"

Write-Host ""
Write-Host "📝 Step 5: Force pushing to remote..." -ForegroundColor Cyan
git push origin master --force

Write-Host ""
Write-Host "✅ Done! Git history has been cleaned." -ForegroundColor Green
Write-Host ""
Write-Host "📋 IMPORTANT: Next steps:" -ForegroundColor Yellow
Write-Host "1. Revoke your Google OAuth credentials at: https://console.cloud.google.com/apis/credentials" -ForegroundColor White
Write-Host "2. Create NEW OAuth credentials" -ForegroundColor White
Write-Host "3. Update your local .env file with the NEW credentials" -ForegroundColor White
Write-Host "4. Tell any collaborators to re-clone the repository" -ForegroundColor White
