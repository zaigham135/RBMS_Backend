# Fix RDS Region Mismatch

## Problem
Your RDS database is in `eu-north-1` but your Elastic Beanstalk is in `us-east-1`. They must be in the same region.

## Solution: Recreate RDS in us-east-1

---

## Step 1: Delete the RDS in eu-north-1

### Option A: Using AWS Console (Easier)
1. Go to AWS Console → RDS
2. **IMPORTANT**: Switch region to **eu-north-1** (top right corner)
3. Select `taskmanagement-db`
4. Click **Actions** → **Delete**
5. Uncheck "Create final snapshot" (since this is a new database with no data)
6. Check "I acknowledge..."
7. Type `delete me` in the confirmation box
8. Click **Delete**

### Option B: Using AWS CLI
```powershell
aws rds delete-db-instance `
  --db-instance-identifier taskmanagement-db `
  --skip-final-snapshot `
  --region eu-north-1
```

---

## Step 2: Create RDS in us-east-1

### Using AWS Console (Recommended - you can see all options)

1. Go to AWS Console → RDS
2. **IMPORTANT**: Switch region to **us-east-1** (top right corner)
3. Click **Create database**

**Engine options:**
- Engine type: **PostgreSQL**
- Version: Latest (default)

**Templates:**
- Select: **Free tier** ✓

**Settings:**
- DB instance identifier: `taskmanagement-db`
- Master username: `postgres`
- Master password: (create a strong password - SAVE THIS!)
- Confirm password: (same password)

**Instance configuration:**
- DB instance class: `db.t3.micro` (should be pre-selected with Free tier)

**Storage:**
- Storage type: `General Purpose SSD (gp2)`
- Allocated storage: `20` GiB
- **Uncheck** "Enable storage autoscaling"

**Connectivity:**
- Virtual private cloud (VPC): Select `taskmanagement-vpc` (the one you created)
- DB subnet group: Create new or use existing
- Public access: **No**
- VPC security group: Choose existing
  - Remove default security group
  - Add `taskmanagement-rds-sg` (the one you created earlier)
- Availability Zone: No preference

**Database authentication:**
- Password authentication (default)

**Additional configuration:**
- Initial database name: `taskmanagement` ⚠️ IMPORTANT - don't skip this!
- Backup retention period: 7 days
- **Uncheck** "Enable encryption" (not available in free tier)
- **Uncheck** "Enable Enhanced monitoring"
- **Uncheck** "Enable deletion protection" (for testing)

4. Click **Create database**
5. Wait 5-10 minutes for status to become "Available"

---

## Step 3: Get the New RDS Endpoint

After the database is created and available:

### Option A: AWS Console
1. Go to RDS → Databases → taskmanagement-db
2. Copy the **Endpoint** value (should end with `.us-east-1.rds.amazonaws.com`)

### Option B: AWS CLI
```powershell
aws rds describe-db-instances `
  --db-instance-identifier taskmanagement-db `
  --region us-east-1 `
  --query 'DBInstances[0].Endpoint.Address' `
  --output text
```

**Save this endpoint!** You'll need it in the next step.

---

## Step 4: Create Parameter Store Parameters

Now create the parameters in us-east-1:

```powershell
# Database URL (replace YOUR-NEW-ENDPOINT with the endpoint from Step 3)
aws ssm put-parameter `
  --name "/taskmanagement/prod/db/url" `
  --value "jdbc:postgresql://YOUR-NEW-ENDPOINT:5432/taskmanagement" `
  --type String `
  --region us-east-1

# Database username
aws ssm put-parameter `
  --name "/taskmanagement/prod/db/username" `
  --value "postgres" `
  --type String `
  --region us-east-1

# Database password (replace YOUR-DB-PASSWORD)
aws ssm put-parameter `
  --name "/taskmanagement/prod/db/password" `
  --value "YOUR-DB-PASSWORD" `
  --type SecureString `
  --region us-east-1

# AWS Region
aws ssm put-parameter `
  --name "/taskmanagement/prod/aws/region" `
  --value "us-east-1" `
  --type String `
  --region us-east-1

# Mail username (replace with your Gmail)
aws ssm put-parameter `
  --name "/taskmanagement/prod/mail/username" `
  --value "your-email@gmail.com" `
  --type String `
  --region us-east-1

# Mail password (replace with your Gmail App Password)
aws ssm put-parameter `
  --name "/taskmanagement/prod/mail/password" `
  --value "YOUR-GMAIL-APP-PASSWORD" `
  --type SecureString `
  --region us-east-1

# Google OAuth Client ID (replace with your actual value or use placeholder)
aws ssm put-parameter `
  --name "/taskmanagement/prod/google/client-id" `
  --value "YOUR-CLIENT-ID.apps.googleusercontent.com" `
  --type String `
  --region us-east-1

# Google OAuth Client Secret (replace with your actual value or use placeholder)
aws ssm put-parameter `
  --name "/taskmanagement/prod/google/client-secret" `
  --value "YOUR-CLIENT-SECRET" `
  --type SecureString `
  --region us-east-1

# ImageKit Public Key
aws ssm put-parameter `
  --name "/taskmanagement/prod/imagekit/public-key" `
  --value "YOUR-IMAGEKIT-PUBLIC-KEY" `
  --type String `
  --region us-east-1

# ImageKit Private Key
aws ssm put-parameter `
  --name "/taskmanagement/prod/imagekit/private-key" `
  --value "YOUR-IMAGEKIT-PRIVATE-KEY" `
  --type SecureString `
  --region us-east-1

# ImageKit URL Endpoint
aws ssm put-parameter `
  --name "/taskmanagement/prod/imagekit/url-endpoint" `
  --value "https://ik.imagekit.io/rx5x7e5fu" `
  --type String `
  --region us-east-1
```

---

## Step 5: Set Environment Variables in Elastic Beanstalk

After creating all parameters, set the environment variables:

```powershell
# Get values from Parameter Store
$DB_ENDPOINT = aws ssm get-parameter --name "/taskmanagement/prod/db/url" --query 'Parameter.Value' --output text --region us-east-1
$DB_USERNAME = aws ssm get-parameter --name "/taskmanagement/prod/db/username" --query 'Parameter.Value' --output text --region us-east-1
$DB_PASSWORD = aws ssm get-parameter --name "/taskmanagement/prod/db/password" --with-decryption --query 'Parameter.Value' --output text --region us-east-1
$MAIL_USERNAME = aws ssm get-parameter --name "/taskmanagement/prod/mail/username" --query 'Parameter.Value' --output text --region us-east-1
$MAIL_PASSWORD = aws ssm get-parameter --name "/taskmanagement/prod/mail/password" --with-decryption --query 'Parameter.Value' --output text --region us-east-1
$GOOGLE_CLIENT_ID = aws ssm get-parameter --name "/taskmanagement/prod/google/client-id" --query 'Parameter.Value' --output text --region us-east-1
$GOOGLE_CLIENT_SECRET = aws ssm get-parameter --name "/taskmanagement/prod/google/client-secret" --with-decryption --query 'Parameter.Value' --output text --region us-east-1
$IMAGEKIT_PUBLIC_KEY = aws ssm get-parameter --name "/taskmanagement/prod/imagekit/public-key" --query 'Parameter.Value' --output text --region us-east-1
$IMAGEKIT_PRIVATE_KEY = aws ssm get-parameter --name "/taskmanagement/prod/imagekit/private-key" --with-decryption --query 'Parameter.Value' --output text --region us-east-1
$IMAGEKIT_URL_ENDPOINT = aws ssm get-parameter --name "/taskmanagement/prod/imagekit/url-endpoint" --query 'Parameter.Value' --output text --region us-east-1

# Set environment variables
eb setenv `
  DB_URL="$DB_ENDPOINT" `
  DB_USERNAME="$DB_USERNAME" `
  DB_PASSWORD="$DB_PASSWORD" `
  AWS_REGION="us-east-1" `
  MAIL_USERNAME="$MAIL_USERNAME" `
  MAIL_PASSWORD="$MAIL_PASSWORD" `
  GOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" `
  GOOGLE_CLIENT_SECRET="$GOOGLE_CLIENT_SECRET" `
  IMAGEKIT_PUBLIC_KEY="$IMAGEKIT_PUBLIC_KEY" `
  IMAGEKIT_PRIVATE_KEY="$IMAGEKIT_PRIVATE_KEY" `
  IMAGEKIT_URL_ENDPOINT="$IMAGEKIT_URL_ENDPOINT" `
  GOOGLE_CALENDAR_ENABLED="false" `
  SESSION_TIMEOUT="1800"
```

---

## Step 6: Verify

1. Wait 2-3 minutes for environment update
2. Check status:
   ```powershell
   eb status
   ```
3. Check health (should be Green):
   ```powershell
   eb health
   ```
4. Test health endpoint:
   ```powershell
   $url = (eb status | Select-String "CNAME:" | ForEach-Object { $_.ToString().Split(":")[1].Trim() })
   curl "http://$url/actuator/health"
   ```

Expected: `{"status":"UP"}`

---

## Quick Summary

1. ✅ Delete RDS in eu-north-1
2. ✅ Create RDS in us-east-1 with Free tier template
3. ✅ Get new endpoint
4. ✅ Create Parameter Store parameters in us-east-1
5. ✅ Set EB environment variables
6. ✅ Verify health is Green

---

## Need Help?

Let me know when you:
- Complete Step 1 (delete old RDS)
- Complete Step 2 (create new RDS in us-east-1)
- Have the new endpoint

I'll help you with the remaining steps!
