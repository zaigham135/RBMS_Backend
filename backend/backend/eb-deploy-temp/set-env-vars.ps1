# Script to set Elastic Beanstalk environment variables
# Replace the placeholder values with your actual values before running

Write-Host "Setting Elastic Beanstalk Environment Variables..." -ForegroundColor Green
Write-Host "IMPORTANT: Edit this script and replace placeholder values first!" -ForegroundColor Yellow
Write-Host ""

# ============================================================================
# EDIT THESE VALUES BEFORE RUNNING
# ============================================================================

# Database Configuration (from RDS)
$DB_ENDPOINT = "YOUR-RDS-ENDPOINT.rds.amazonaws.com"  # e.g., taskmanagement-db.xxxxx.us-east-1.rds.amazonaws.com
$DB_PASSWORD = "YOUR-DATABASE-PASSWORD"                # The password you set when creating RDS

# AWS Configuration
$AWS_REGION = "us-east-1"

# Email Configuration (from Parameter Store - you already created these)
$MAIL_USERNAME = "YOUR-GMAIL-ADDRESS"                  # e.g., yourname@gmail.com
$MAIL_PASSWORD = "YOUR-GMAIL-APP-PASSWORD"             # Gmail App Password (16 characters)

# Google OAuth (from Parameter Store - you already created these)
$GOOGLE_CLIENT_ID = "YOUR-GOOGLE-CLIENT-ID"
$GOOGLE_CLIENT_SECRET = "YOUR-GOOGLE-CLIENT-SECRET"

# ImageKit Configuration (from Parameter Store - you already created these)
$IMAGEKIT_PUBLIC_KEY = "YOUR-IMAGEKIT-PUBLIC-KEY"
$IMAGEKIT_PRIVATE_KEY = "YOUR-IMAGEKIT-PRIVATE-KEY"
$IMAGEKIT_URL_ENDPOINT = "https://ik.imagekit.io/rx5x7e5fu"

# ============================================================================
# DO NOT EDIT BELOW THIS LINE
# ============================================================================

# Construct database URL
$DB_URL = "jdbc:postgresql://${DB_ENDPOINT}:5432/taskmanagement"

Write-Host "Database URL: $DB_URL" -ForegroundColor Cyan
Write-Host ""
Write-Host "Setting environment variables..." -ForegroundColor Cyan

# Set all environment variables at once
eb setenv `
  DB_URL="$DB_URL" `
  DB_USERNAME="postgres" `
  DB_PASSWORD="$DB_PASSWORD" `
  AWS_REGION="$AWS_REGION" `
  MAIL_USERNAME="$MAIL_USERNAME" `
  MAIL_PASSWORD="$MAIL_PASSWORD" `
  GOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" `
  GOOGLE_CLIENT_SECRET="$GOOGLE_CLIENT_SECRET" `
  IMAGEKIT_PUBLIC_KEY="$IMAGEKIT_PUBLIC_KEY" `
  IMAGEKIT_PRIVATE_KEY="$IMAGEKIT_PRIVATE_KEY" `
  IMAGEKIT_URL_ENDPOINT="$IMAGEKIT_URL_ENDPOINT" `
  GOOGLE_CALENDAR_ENABLED="false" `
  SESSION_TIMEOUT="1800"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Environment variables set successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "The environment will now update. This may take a few minutes." -ForegroundColor Yellow
    Write-Host "Check status with: eb status" -ForegroundColor Cyan
    Write-Host "Check health with: eb health" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "✗ Failed to set environment variables!" -ForegroundColor Red
    Write-Host "Please check the error message above." -ForegroundColor Red
}
