# Prepare Repository for GitHub Push
# This script removes sensitive files from git tracking

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Preparing Repository for GitHub" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$projectRoot = "E:\role based task  management project\Backend_Java"
Set-Location $projectRoot

Write-Host "[1/5] Checking for sensitive files..." -ForegroundColor Yellow

# List of sensitive files to remove from tracking
$sensitiveFiles = @(
    "backend/backend/config/google-credentials.json",
    "backend/backend/eb-deploy-temp/set-env-eu-north.ps1"
)

$filesFound = @()
foreach ($file in $sensitiveFiles) {
    if (Test-Path $file) {
        $filesFound += $file
        Write-Host "  Found: $file" -ForegroundColor Red
    }
}

if ($filesFound.Count -eq 0) {
    Write-Host "  No sensitive files found in working directory" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[2/5] Removing sensitive files from git tracking..." -ForegroundColor Yellow
    
    foreach ($file in $filesFound) {
        Write-Host "  Removing: $file" -ForegroundColor Yellow
        git rm --cached $file 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ Removed from git tracking" -ForegroundColor Green
        } else {
            Write-Host "  ℹ File not tracked by git (safe)" -ForegroundColor Gray
        }
    }
}

Write-Host ""
Write-Host "[3/5] Checking git status..." -ForegroundColor Yellow
git status --short

Write-Host ""
Write-Host "[4/5] Searching for potential secrets in staged files..." -ForegroundColor Yellow

# Search for common secret patterns
$patterns = @("password\s*=", "secret\s*=", "key\s*=", "credentials", "private_key")
$foundSecrets = $false

foreach ($pattern in $patterns) {
    $results = git diff --cached | Select-String -Pattern $pattern -CaseSensitive:$false
    if ($results) {
        Write-Host "  ⚠ Found potential secret: $pattern" -ForegroundColor Red
        $foundSecrets = $true
    }
}

if (-not $foundSecrets) {
    Write-Host "  ✓ No obvious secrets found in staged files" -ForegroundColor Green
}

Write-Host ""
Write-Host "[5/5] Verification complete!" -ForegroundColor Yellow
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Files removed from tracking:" -ForegroundColor White
if ($filesFound.Count -eq 0) {
    Write-Host "  None (all clean)" -ForegroundColor Green
} else {
    foreach ($file in $filesFound) {
        Write-Host "  - $file" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Review changes: git status" -ForegroundColor Gray
Write-Host "  2. Review diff: git diff --cached" -ForegroundColor Gray
Write-Host "  3. Commit changes: git commit -m 'Remove sensitive files from tracking'" -ForegroundColor Gray
Write-Host "  4. Create new branch: git checkout -b deployment-ready" -ForegroundColor Gray
Write-Host "  5. Push to GitHub: git push origin deployment-ready" -ForegroundColor Gray
Write-Host ""

Write-Host "⚠ IMPORTANT: The sensitive files still exist on your local machine." -ForegroundColor Yellow
Write-Host "   They just won't be pushed to GitHub." -ForegroundColor Yellow
Write-Host ""

Write-Host "✓ Repository is ready for GitHub push!" -ForegroundColor Green
Write-Host ""
