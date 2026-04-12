# Security Verification Script
# Checks for sensitive data before GitHub push

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Security Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "E:\role based task  management project\Backend_Java"
Set-Location $projectRoot

$issues = @()

# Check 1: Sensitive files in git
Write-Host "[1/6] Checking for sensitive files in git..." -ForegroundColor Yellow

$sensitivePatterns = @(
    "*credentials*.json",
    "*.env",
    "*secret*",
    "*password*",
    "*.pem",
    "*.key",
    "*set-env*.ps1"
)

$trackedSensitiveFiles = @()
foreach ($pattern in $sensitivePatterns) {
    $files = git ls-files | Select-String -Pattern $pattern -SimpleMatch:$false
    if ($files) {
        $trackedSensitiveFiles += $files
    }
}

if ($trackedSensitiveFiles.Count -gt 0) {
    Write-Host "  ⚠ WARNING: Sensitive files tracked by git:" -ForegroundColor Red
    foreach ($file in $trackedSensitiveFiles) {
        Write-Host "    - $file" -ForegroundColor Red
    }
    $issues += "Sensitive files tracked by git"
} else {
    Write-Host "  ✓ No sensitive files tracked" -ForegroundColor Green
}

# Check 2: .gitignore exists
Write-Host ""
Write-Host "[2/6] Checking .gitignore files..." -ForegroundColor Yellow

$gitignoreFiles = @(
    ".gitignore",
    "backend/backend/.gitignore",
    "../Nextjs_Frontend/.gitignore"
)

foreach ($file in $gitignoreFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ Found: $file" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Missing: $file" -ForegroundColor Red
        $issues += "Missing .gitignore: $file"
    }
}

# Check 3: Hardcoded secrets in code
Write-Host ""
Write-Host "[3/6] Scanning for hardcoded secrets..." -ForegroundColor Yellow

$secretPatterns = @(
    'password\s*=\s*["\'][^"\']+["\']',
    'secret\s*=\s*["\'][^"\']+["\']',
    'api[_-]?key\s*=\s*["\'][^"\']+["\']',
    'private[_-]?key\s*=\s*["\'][^"\']+["\']'
)

$foundSecrets = $false
foreach ($pattern in $secretPatterns) {
    $results = Get-ChildItem -Path . -Include *.java,*.ts,*.tsx,*.js -Recurse -ErrorAction SilentlyContinue | 
               Select-String -Pattern $pattern -CaseSensitive:$false |
               Where-Object { $_.Path -notmatch "node_modules|target|\.next|\.git" }
    
    if ($results) {
        Write-Host "  ⚠ Found potential secret: $pattern" -ForegroundColor Red
        $foundSecrets = $true
        $issues += "Hardcoded secret found"
    }
}

if (-not $foundSecrets) {
    Write-Host "  ✓ No obvious hardcoded secrets found" -ForegroundColor Green
}

# Check 4: Google credentials file
Write-Host ""
Write-Host "[4/6] Checking Google credentials..." -ForegroundColor Yellow

$googleCredsPath = "backend/backend/config/google-credentials.json"
if (Test-Path $googleCredsPath) {
    $isTracked = git ls-files $googleCredsPath
    if ($isTracked) {
        Write-Host "  ⚠ WARNING: google-credentials.json is tracked by git!" -ForegroundColor Red
        $issues += "Google credentials tracked by git"
    } else {
        Write-Host "  ✓ google-credentials.json exists but not tracked" -ForegroundColor Green
    }
} else {
    Write-Host "  ℹ google-credentials.json not found (may be okay)" -ForegroundColor Gray
}

# Check 5: Environment files
Write-Host ""
Write-Host "[5/6] Checking environment files..." -ForegroundColor Yellow

$envFiles = Get-ChildItem -Path . -Filter ".env*" -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -notmatch "node_modules|target|\.next|\.git" }

$trackedEnvFiles = @()
foreach ($envFile in $envFiles) {
    $relativePath = $envFile.FullName.Replace($projectRoot + "\", "").Replace("\", "/")
    $isTracked = git ls-files $relativePath
    if ($isTracked) {
        $trackedEnvFiles += $relativePath
    }
}

if ($trackedEnvFiles.Count -gt 0) {
    Write-Host "  ⚠ WARNING: .env files tracked by git:" -ForegroundColor Red
    foreach ($file in $trackedEnvFiles) {
        Write-Host "    - $file" -ForegroundColor Red
    }
    $issues += ".env files tracked by git"
} else {
    Write-Host "  ✓ No .env files tracked" -ForegroundColor Green
}

# Check 6: Staged files review
Write-Host ""
Write-Host "[6/6] Reviewing staged files..." -ForegroundColor Yellow

$stagedFiles = git diff --cached --name-only
if ($stagedFiles) {
    Write-Host "  Files staged for commit:" -ForegroundColor White
    foreach ($file in $stagedFiles) {
        Write-Host "    - $file" -ForegroundColor Gray
    }
} else {
    Write-Host "  ℹ No files staged for commit" -ForegroundColor Gray
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Verification Results" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($issues.Count -eq 0) {
    Write-Host "✅ ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Your repository is secure and ready to push to GitHub." -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor White
    Write-Host "  1. git add ." -ForegroundColor Gray
    Write-Host "  2. git commit -m 'Prepare for deployment'" -ForegroundColor Gray
    Write-Host "  3. git checkout -b deployment-ready" -ForegroundColor Gray
    Write-Host "  4. git push origin deployment-ready" -ForegroundColor Gray
} else {
    Write-Host "⚠ ISSUES FOUND: $($issues.Count)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Issues to fix:" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "  - $issue" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Run this to fix:" -ForegroundColor Yellow
    Write-Host "  .\prepare-for-github.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "⚠ DO NOT PUSH until issues are resolved!" -ForegroundColor Red
}

Write-Host ""
