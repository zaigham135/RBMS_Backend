# Recreate Elastic Beanstalk Environment with Correct VPC

Write-Host "Recreating EB Environment with VPC Configuration..." -ForegroundColor Green
Write-Host ""

# Step 1: Get VPC ID
Write-Host "[1/6] Getting VPC ID..." -ForegroundColor Cyan
$VPC_ID = aws ec2 describe-vpcs --filters "Name=tag:Name,Values=taskmanagement-vpc" --query "Vpcs[0].VpcId" --output text --region eu-north-1

if ($VPC_ID -eq "None" -or [string]::IsNullOrEmpty($VPC_ID)) {
    Write-Host "ERROR: taskmanagement-vpc not found!" -ForegroundColor Red
    Write-Host "Please create the VPC first or check the VPC name." -ForegroundColor Yellow
    exit 1
}

Write-Host "VPC ID: $VPC_ID" -ForegroundColor Green

# Step 2: Get Subnet IDs
Write-Host ""
Write-Host "[2/6] Getting Subnet IDs..." -ForegroundColor Cyan
$SUBNETS = aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query "Subnets[*].SubnetId" --output text --region eu-north-1
$SUBNET_ARRAY = $SUBNETS -split '\s+'

if ($SUBNET_ARRAY.Count -eq 0) {
    Write-Host "ERROR: No subnets found in VPC!" -ForegroundColor Red
    exit 1
}

Write-Host "Found $($SUBNET_ARRAY.Count) subnets: $SUBNETS" -ForegroundColor Green

# Step 3: Get Security Group ID
Write-Host ""
Write-Host "[3/6] Getting Security Group ID..." -ForegroundColor Cyan
$SG_ID = aws ec2 describe-security-groups --filters "Name=group-name,Values=taskmanagement-backend-sg" "Name=vpc-id,Values=$VPC_ID" --query "SecurityGroups[0].GroupId" --output text --region eu-north-1

if ($SG_ID -eq "None" -or [string]::IsNullOrEmpty($SG_ID)) {
    Write-Host "WARNING: taskmanagement-backend-sg not found. Will use default." -ForegroundColor Yellow
    $SG_ID = ""
} else {
    Write-Host "Security Group ID: $SG_ID" -ForegroundColor Green
}

# Step 4: Terminate existing environment
Write-Host ""
Write-Host "[4/6] Terminating existing environment..." -ForegroundColor Cyan
Write-Host "This will take 2-3 minutes..." -ForegroundColor Yellow
eb terminate taskmanagement-backend-prod --force

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to terminate environment!" -ForegroundColor Red
    exit 1
}

Write-Host "Environment terminated successfully" -ForegroundColor Green

# Step 5: Create .ebextensions/vpc.config
Write-Host ""
Write-Host "[5/6] Creating VPC configuration..." -ForegroundColor Cyan

$vpcConfig = @"
option_settings:
  aws:ec2:vpc:
    VPCId: $VPC_ID
    Subnets: $($SUBNET_ARRAY -join ',')
    ELBSubnets: $($SUBNET_ARRAY -join ',')
"@

if (-not [string]::IsNullOrEmpty($SG_ID)) {
    $vpcConfig += @"

    SecurityGroups: $SG_ID
"@
}

Set-Content -Path ".ebextensions/vpc.config" -Value $vpcConfig
Write-Host "VPC configuration created" -ForegroundColor Green

# Step 6: Create new environment
Write-Host ""
Write-Host "[6/6] Creating new environment with VPC..." -ForegroundColor Cyan
Write-Host "This will take 5-10 minutes..." -ForegroundColor Yellow

eb create taskmanagement-backend-prod --instance-type t3.micro

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ Environment created successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Set environment variables: ./set-env-eu-north.ps1" -ForegroundColor White
    Write-Host "2. Check health: eb health" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "✗ Failed to create environment!" -ForegroundColor Red
}
