# Simple solution: Create a new branch without the problematic commits

Write-Host "🔒 Creating clean branch without secrets..." -ForegroundColor Yellow
Write-Host ""

# Step 1: Create a new orphan branch (no history)
Write-Host "📝 Step 1: Creating new orphan branch..." -ForegroundColor Cyan
git checkout --orphan clean-master

# Step 2: Add all current files
Write-Host "📝 Step 2: Adding all current files..." -ForegroundColor Cyan
git add -A

# Step 3: Commit with clean history
Write-Host "📝 Step 3: Creating initial commit..." -ForegroundColor Cyan
git commit -m "Initial commit - secrets removed"

# Step 4: Delete old master and rename clean-master to master
Write-Host "📝 Step 4: Replacing old master branch..." -ForegroundColor Cyan
git branch -D master
git branch -m master

# Step 5: Force push to remote
Write-Host ""
Write-Host "⚠️  WARNING: This will replace the entire Git history!" -ForegroundColor Red
Write-Host "Continue? (Y/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -eq 'Y' -or $response -eq 'y') {
    Write-Host ""
    Write-Host "📝 Step 5: Force pushing to remote..." -ForegroundColor Cyan
    git push origin master --force
    
    Write-Host ""
    Write-Host "✅ Done! Clean history pushed to GitHub." -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 CRITICAL: Security steps:" -ForegroundColor Red
    Write-Host "1. Go to: https://console.cloud.google.com/apis/credentials" -ForegroundColor White
    Write-Host "2. DELETE the exposed OAuth client (702011085337-...)" -ForegroundColor White
    Write-Host "3. CREATE a new OAuth 2.0 Client ID" -ForegroundColor White
    Write-Host "4. Update your .env file with NEW credentials" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "Aborted. Run git checkout master to go back." -ForegroundColor Red
}
