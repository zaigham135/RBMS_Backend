# Elastic Beanstalk Deployment Script
# This script creates a proper deployment package for EB

Write-Host "Starting Elastic Beanstalk deployment preparation..." -ForegroundColor Green

# Step 1: Clean and build the application
Write-Host "`n[1/5] Building application..." -ForegroundColor Cyan
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Step 2: Create deployment directory
Write-Host "`n[2/5] Creating deployment package..." -ForegroundColor Cyan
$deployDir = "eb-deploy-temp"
if (Test-Path $deployDir) {
    Remove-Item -Recurse -Force $deployDir
}
New-Item -ItemType Directory -Path $deployDir | Out-Null

# Step 3: Copy JAR file to root of deployment directory
Write-Host "`n[3/5] Copying JAR file..." -ForegroundColor Cyan
$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" | Select-Object -First 1
if (-not $jarFile) {
    Write-Host "No JAR file found in target directory!" -ForegroundColor Red
    exit 1
}
Copy-Item $jarFile.FullName -Destination "$deployDir/application.jar"
Write-Host "Copied: $($jarFile.Name) -> application.jar" -ForegroundColor Green

# Step 4: Copy .ebextensions directory
Write-Host "`n[4/5] Copying .ebextensions..." -ForegroundColor Cyan
if (Test-Path ".ebextensions") {
    Copy-Item -Recurse ".ebextensions" -Destination "$deployDir/.ebextensions"
    Write-Host "Copied .ebextensions directory" -ForegroundColor Green
} else {
    Write-Host "Warning: .ebextensions directory not found" -ForegroundColor Yellow
}

# Step 5: Create Procfile
Write-Host "`n[5/5] Creating Procfile..." -ForegroundColor Cyan
$procfileContent = "web: java -jar application.jar"
Set-Content -Path "$deployDir/Procfile" -Value $procfileContent
Write-Host "Created Procfile" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Deployment package ready in: $deployDir" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. cd $deployDir" -ForegroundColor White
Write-Host "2. eb init (if not already initialized)" -ForegroundColor White
Write-Host "3. eb create taskmanagement-backend-prod --instance-type t3.micro" -ForegroundColor White
Write-Host "   OR" -ForegroundColor White
Write-Host "   eb deploy (if environment already exists)" -ForegroundColor White
