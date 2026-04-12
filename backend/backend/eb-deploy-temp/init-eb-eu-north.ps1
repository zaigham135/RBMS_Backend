# Initialize Elastic Beanstalk for eu-north-1 region

Write-Host "Initializing Elastic Beanstalk for eu-north-1..." -ForegroundColor Green

# Create .elasticbeanstalk directory
New-Item -ItemType Directory -Force -Path ".elasticbeanstalk" | Out-Null

# Create config.yml
$configContent = @"
branch-defaults:
  default:
    environment: taskmanagement-backend-prod
global:
  application_name: taskmanagement-backend
  default_platform: Corretto 17 running on 64bit Amazon Linux 2023
  default_region: eu-north-1
  sc: null
"@

Set-Content -Path ".elasticbeanstalk/config.yml" -Value $configContent

Write-Host "✓ EB configuration created for eu-north-1" -ForegroundColor Green
Write-Host ""
Write-Host "Next step: Create the environment" -ForegroundColor Yellow
Write-Host "Run: eb create taskmanagement-backend-prod --instance-type t3.micro" -ForegroundColor Cyan
