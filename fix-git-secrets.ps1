# Script to remove secrets from Git history
# Run this from Backend_Java directory

Write-Host "🔒 Removing secrets from Git history..." -ForegroundColor Yellow

# Step 1: Remove the file from Git history
Write-Host "`n📝 Step 1: Removing application.properties from Git cache..." -ForegroundColor Cyan
git rm --cached backend/backend/src/main/resources/application.properties

# Step 2: Add the updated file
Write-Host "`n📝 Step 2: Adding updated application.properties..." -ForegroundColor Cyan
git add backend/backend/src/main/resources/application.properties
git add .gitignore
git add backend/backend/.env.example

# Step 3: Amend the last commit
Write-Host "`n📝 Step 3: Amending commit to remove secrets..." -ForegroundColor Cyan
git commit --amend -m "Remove hardcoded secrets from application.properties"

# Step 4: Force push (WARNING: This rewrites history)
Write-Host "`n⚠️  Step 4: Force pushing to remote..." -ForegroundColor Red
Write-Host "This will rewrite Git history. Continue? (Y/N)" -ForegroundColor Yellow
$response = Read-Host
if ($response -eq 'Y' -or $response -eq 'y') {
    git push origin master --force
    Write-Host "`n✅ Done! Secrets removed from Git history." -ForegroundColor Green
} else {
    Write-Host "`n❌ Aborted. You can manually run: git push origin master --force" -ForegroundColor Red
}

Write-Host "`n📋 Next steps:" -ForegroundColor Cyan
Write-Host "1. Create a .env file in backend/backend/ directory" -ForegroundColor White
Write-Host "2. Copy values from .env.example and fill in your actual secrets" -ForegroundColor White
Write-Host "3. Never commit the .env file to Git" -ForegroundColor White
