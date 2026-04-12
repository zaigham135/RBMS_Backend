# Security Checklist Before Pushing to GitHub

## ⚠️ CRITICAL: Files to Remove/Check Before Push

### 1. Google Credentials File
**Location**: `Backend_Java/backend/backend/config/google-credentials.json`

**Status**: ⚠️ **CURRENTLY TRACKED IN GIT**

**Action Required**:
```powershell
# Remove from git tracking
cd "E:\role based task  management project\Backend_Java"
git rm --cached backend/backend/config/google-credentials.json

# Verify it's in .gitignore (already added)
# The file will remain on your local machine but won't be pushed to GitHub
```

### 2. AWS Deployment Scripts with Credentials
**Location**: `Backend_Java/backend/backend/eb-deploy-temp/set-env-eu-north.ps1`

**Contains**:
- Database passwords
- Email passwords
- OAuth secrets
- API keys

**Action Required**:
```powershell
# Remove from git tracking
git rm --cached backend/backend/eb-deploy-temp/set-env-eu-north.ps1

# Already added to .gitignore as **/set-env*.ps1
```

### 3. Environment Files
**Check for**:
- `.env`
- `.env.local`
- `.env.production`
- Any `*.env` files

**Status**: ✅ Already in `.gitignore`

## Files That Are Safe to Push

✅ `.gitignore` files (updated with security patterns)
✅ Source code files (`.java`, `.tsx`, `.ts`)
✅ Configuration templates (without actual secrets)
✅ Documentation files (`.md`)
✅ Package files (`package.json`, `pom.xml`)

## Updated .gitignore Patterns

### Backend (.gitignore)
```
# Secrets and credentials
**/google-credentials.json
**/*credentials*.json
**/*secret*.json
*.pem
*.key
*.p12
*.jks
*.keystore
**/secrets/
**/credentials/

# AWS Credentials
.aws/
**/.aws/

# Deployment Scripts with Sensitive Data
**/set-env*.ps1
**/set-env*.sh

# Environment variables
.env
*.env
!.env.example
```

### Frontend (.gitignore)
```
# Local env files
.env
.env*.local
.env.local
.env.development.local
.env.test.local
.env.production.local
*.env
```

## Pre-Push Commands

Run these commands before pushing:

```powershell
# Navigate to project root
cd "E:\role based task  management project\Backend_Java"

# Remove sensitive files from git tracking
git rm --cached backend/backend/config/google-credentials.json
git rm --cached backend/backend/eb-deploy-temp/set-env-eu-north.ps1

# Check what will be committed
git status

# Verify no sensitive files are staged
git diff --cached --name-only

# Search for potential secrets in staged files
git diff --cached | Select-String -Pattern "password|secret|key|credentials"
```

## Verification Steps

### 1. Check for Secrets in Code
```powershell
# Search for hardcoded secrets
cd "E:\role based task  management project"
Select-String -Path "**/*.java","**/*.ts","**/*.tsx" -Pattern "password\s*=|secret\s*=|key\s*=" -Exclude "*.md"
```

### 2. Verify .gitignore is Working
```powershell
# This should show google-credentials.json as ignored
git status --ignored

# This should NOT show google-credentials.json
git status
```

### 3. Check Staged Files
```powershell
# List all files that will be pushed
git diff --cached --name-only

# Review the actual changes
git diff --cached
```

## What to Do If You Accidentally Push Secrets

### If you haven't pushed yet:
```powershell
# Unstage the file
git reset HEAD <file-with-secret>

# Remove from git history
git rm --cached <file-with-secret>

# Commit the removal
git commit -m "Remove sensitive file from tracking"
```

### If you already pushed:
1. **Immediately rotate all exposed credentials**
2. **Remove from git history** using:
   ```powershell
   git filter-branch --force --index-filter "git rm --cached --ignore-unmatch <file-path>" --prune-empty --tag-name-filter cat -- --all
   ```
3. **Force push** (⚠️ dangerous if others are using the repo):
   ```powershell
   git push origin --force --all
   ```

## Safe Files to Include

### Configuration Templates
Create example files without real secrets:

**Example: `.env.example`**
```
DB_URL=jdbc:postgresql://localhost:5432/dbname
DB_USERNAME=your_username
DB_PASSWORD=your_password
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
```

### Documentation
- README.md
- Setup guides
- Architecture diagrams
- API documentation

## Final Checklist Before Push

- [ ] Removed `google-credentials.json` from git tracking
- [ ] Removed `set-env-eu-north.ps1` from git tracking
- [ ] Verified `.gitignore` includes all sensitive patterns
- [ ] Ran `git status` - no sensitive files shown
- [ ] Ran `git diff --cached` - reviewed all changes
- [ ] Searched for hardcoded secrets in code
- [ ] Created `.env.example` with placeholder values
- [ ] Updated README with setup instructions (without secrets)

## Recommended: Create .env.example Files

### Backend: `backend/backend/.env.example`
```
# Database
DB_URL=jdbc:postgresql://localhost:5432/task_management
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# Email
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password_here

# Google OAuth
GOOGLE_CLIENT_ID=your_client_id_here
GOOGLE_CLIENT_SECRET=your_client_secret_here

# ImageKit
IMAGEKIT_PUBLIC_KEY=your_public_key_here
IMAGEKIT_PRIVATE_KEY=your_private_key_here
IMAGEKIT_URL_ENDPOINT=your_endpoint_here

# Google Calendar
GOOGLE_CALENDAR_ENABLED=false
GOOGLE_CALENDAR_CREDENTIALS_FILE=/path/to/credentials.json
```

### Frontend: `Nextjs_Frontend/.env.example`
```
# API URL
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## After Pushing

1. **Verify on GitHub**: Check the repository to ensure no sensitive files are visible
2. **Review commit history**: Make sure no secrets in previous commits
3. **Set up GitHub Secrets**: For CI/CD, use GitHub Secrets instead of committing credentials
4. **Enable branch protection**: Require reviews before merging to main

## Additional Security Measures

### 1. Use GitHub Secret Scanning
- GitHub automatically scans for known secret patterns
- Enable in: Repository Settings → Security → Secret scanning

### 2. Use .gitattributes
Create `.gitattributes` to prevent accidental commits:
```
*.env filter=git-crypt diff=git-crypt
*credentials*.json filter=git-crypt diff=git-crypt
```

### 3. Pre-commit Hooks
Install git-secrets or similar tools:
```powershell
# Install git-secrets (requires Git for Windows)
git secrets --install
git secrets --register-aws
```

## Emergency Contacts

If secrets are exposed:
1. **Rotate all credentials immediately**
2. **Revoke OAuth tokens**
3. **Change database passwords**
4. **Regenerate API keys**
5. **Contact your team/security officer**

---

**Remember**: Once pushed to GitHub, assume the secret is compromised. Always rotate credentials if accidentally exposed!
