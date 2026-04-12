# Move Elastic Beanstalk to eu-north-1

## Current Situation
- RDS, VPC, Security Groups, DynamoDB, Parameter Store: **eu-north-1** ✓
- Elastic Beanstalk: **us-east-1** ✗

## Solution: Recreate Elastic Beanstalk in eu-north-1

---

## Step 1: Terminate Current EB Environment in us-east-1

```powershell
eb terminate taskmanagement-backend-prod
```

Type `taskmanagement-backend-prod` when prompted to confirm.

---

## Step 2: Reinitialize EB for eu-north-1

```powershell
# Remove old EB configuration
Remove-Item -Recurse -Force .elasticbeanstalk

# Initialize EB with eu-north-1 region
eb init
```

When prompted:
1. **Select a default region**: Choose `eu-north-1` (Stockholm)
2. **Application name**: `taskmanagement-backend`
3. **Platform**: Java
4. **Platform branch**: Corretto 17
5. **CodeCommit**: No
6. **SSH**: No

---

## Step 3: Update .ebextensions Configuration

The configuration file should already be correct, but let's verify the region:

```powershell
cat .ebextensions/01_java.config
```

Should show:
```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SERVER_PORT: 5000
    SPRING_PROFILES_ACTIVE: prod
    AWS_REGION: eu-north-1
    JAVA_TOOL_OPTIONS: "-Xmx512m -Xms256m"
```

If AWS_REGION shows us-east-1, update it to eu-north-1.

---

## Step 4: Create EB Environment in eu-north-1

```powershell
eb create taskmanagement-backend-prod --instance-type t3.micro --region eu-north-1
```

This will:
- Create a new environment in eu-north-1
- Deploy your application
- Take 5-10 minutes

---

## Step 5: Configure VPC and Security Groups via Console

After the environment is created, you need to configure it to use your existing VPC and security groups:

1. Go to AWS Console → Elastic Beanstalk
2. **Switch region to eu-north-1**
3. Click on `taskmanagement-backend-prod` environment
4. Click **Configuration** (left sidebar)
5. Find **Network** section → Click **Edit**
6. **VPC**: Select `taskmanagement-vpc`
7. **Instance subnets**: Select your **public subnets** (check both)
8. **Instance security groups**: Select `taskmanagement-backend-sg`
9. Click **Apply**
10. Wait for environment to update (2-3 minutes)

---

## Step 6: Add IAM Instance Profile via Console

1. Still in Configuration page
2. Find **Security** section → Click **Edit**
3. **IAM instance profile**: Select `taskmanagement-eb-ec2-role`
4. Click **Apply**
5. Wait for environment to update (2-3 minutes)

---

## Step 7: Set Environment Variables

Now set all environment variables. First, let's get values from Parameter Store in eu-north-1:

```powershell
# Get database endpoint
$DB_ENDPOINT = "jdbc:postgresql://taskmanagement-db.clmcow6yccmx.eu-north-1.rds.amazonaws.com:5432/taskmanagement"

# Get other values from Parameter Store (if you created them in eu-north-1)
# If not, you'll need to provide them manually

# Set environment variables
eb setenv `
  DB_URL="$DB_ENDPOINT" `
  DB_USERNAME="postgres" `
  DB_PASSWORD="YOUR-DB-PASSWORD" `
  AWS_REGION="eu-north-1" `
  MAIL_USERNAME="YOUR-GMAIL" `
  MAIL_PASSWORD="YOUR-GMAIL-APP-PASSWORD" `
  GOOGLE_CLIENT_ID="YOUR-CLIENT-ID" `
  GOOGLE_CLIENT_SECRET="YOUR-CLIENT-SECRET" `
  IMAGEKIT_PUBLIC_KEY="YOUR-IMAGEKIT-PUBLIC-KEY" `
  IMAGEKIT_PRIVATE_KEY="YOUR-IMAGEKIT-PRIVATE-KEY" `
  IMAGEKIT_URL_ENDPOINT="https://ik.imagekit.io/rx5x7e5fu" `
  GOOGLE_CALENDAR_ENABLED="false" `
  SESSION_TIMEOUT="1800"
```

**Replace the YOUR-* placeholders with your actual values.**

---

## Step 8: Verify

1. Check status:
   ```powershell
   eb status
   ```

2. Check health (should be Green):
   ```powershell
   eb health
   ```

3. Get the URL and test:
   ```powershell
   $url = (eb status | Select-String "CNAME:" | ForEach-Object { $_.ToString().Split(":")[1].Trim() })
   Write-Host "Backend URL: http://$url"
   curl "http://$url/actuator/health"
   ```

Expected: `{"status":"UP"}`

---

## Important Notes

1. **DynamoDB Table**: Make sure you have `spring-sessions` table in eu-north-1
   - If not, create it: AWS Console → DynamoDB → Create table
   - Table name: `spring-sessions`
   - Partition key: `sessionId` (String)
   - Enable TTL on `expirationTime` attribute

2. **Parameter Store**: If you created parameters in us-east-1, you'll need to recreate them in eu-north-1

3. **IAM Role**: Make sure `taskmanagement-eb-ec2-role` exists and has permissions for:
   - DynamoDB (for sessions)
   - Parameter Store (if using)
   - Elastic Beanstalk policies

---

## Quick Checklist

- [ ] Terminate EB in us-east-1
- [ ] Reinitialize EB for eu-north-1
- [ ] Create EB environment in eu-north-1
- [ ] Configure VPC and security groups
- [ ] Add IAM instance profile
- [ ] Set environment variables
- [ ] Verify health is Green
- [ ] Test health endpoint

---

## Need Help?

Provide me with:
1. Your database password
2. Your Gmail credentials
3. Your Google OAuth credentials (or use placeholders)
4. Your ImageKit credentials

I'll help you set the environment variables!
