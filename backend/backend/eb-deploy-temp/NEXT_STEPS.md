# Next Steps to Fix the Deployment

## Current Status
✅ Application deployed successfully  
❌ Health is RED - Cannot connect to database

## Root Cause
The application is trying to connect to `localhost:5433` but should connect to your RDS database. Environment variables are not set.

---

## Step 1: Create RDS Database (if not done)

If you haven't created the RDS database yet or deleted it:

1. Go to AWS Console → RDS → Create database
2. Choose:
   - **Standard create**
   - **PostgreSQL** (latest version)
   - **Free tier** template
   - DB instance identifier: `taskmanagement-db`
   - Master username: `postgres`
   - Master password: (create a strong password and save it!)
   - Instance class: `db.t3.micro`
   - Storage: 20 GiB
   - VPC: Select your `taskmanagement` VPC
   - Public access: **No**
   - Security group: `taskmanagement-rds-sg`
   - Initial database name: `taskmanagement`
3. Wait 5-10 minutes for it to become "Available"
4. Copy the **Endpoint** (e.g., `taskmanagement-db.xxxxx.us-east-1.rds.amazonaws.com`)

---

## Step 2: Get Your RDS Endpoint

Run this command to get your RDS endpoint:

```powershell
aws rds describe-db-instances --query 'DBInstances[*].[DBInstanceIdentifier,Endpoint.Address]' --output table
```

Copy the endpoint address.

---

## Step 3: Get Credentials from Parameter Store

You already stored these in Parameter Store. Get them using:

```powershell
# Database password
aws ssm get-parameter --name "/taskmanagement/prod/db/password" --with-decryption --query 'Parameter.Value' --output text

# Gmail username
aws ssm get-parameter --name "/taskmanagement/prod/mail/username" --query 'Parameter.Value' --output text

# Gmail password
aws ssm get-parameter --name "/taskmanagement/prod/mail/password" --with-decryption --query 'Parameter.Value' --output text

# Google OAuth Client ID
aws ssm get-parameter --name "/taskmanagement/prod/google/client-id" --query 'Parameter.Value' --output text

# Google OAuth Client Secret
aws ssm get-parameter --name "/taskmanagement/prod/google/client-secret" --with-decryption --query 'Parameter.Value' --output text

# ImageKit Public Key
aws ssm get-parameter --name "/taskmanagement/prod/imagekit/public-key" --query 'Parameter.Value' --output text

# ImageKit Private Key
aws ssm get-parameter --name "/taskmanagement/prod/imagekit/private-key" --with-decryption --query 'Parameter.Value' --output text
```

---

## Step 4: Set Environment Variables

Edit `set-env-vars.ps1` and replace all placeholder values with your actual values from Steps 2 and 3.

Then run:

```powershell
./set-env-vars.ps1
```

This will set all environment variables and trigger an environment update.

---

## Step 5: Wait and Verify

1. Wait 2-3 minutes for the environment to update
2. Check status:
   ```powershell
   eb status
   ```
3. Check health (should be Green):
   ```powershell
   eb health
   ```
4. Test the health endpoint:
   ```powershell
   $url = (eb status | Select-String "CNAME:" | ForEach-Object { $_.ToString().Split(":")[1].Trim() })
   curl "http://$url/actuator/health"
   ```

Expected response: `{"status":"UP"}`

---

## Alternative: Quick Command (if you have all values)

If you have all the values ready, you can set them directly:

```powershell
eb setenv `
  DB_URL="jdbc:postgresql://YOUR-RDS-ENDPOINT:5432/taskmanagement" `
  DB_USERNAME="postgres" `
  DB_PASSWORD="YOUR-DB-PASSWORD" `
  AWS_REGION="us-east-1" `
  MAIL_USERNAME="YOUR-GMAIL" `
  MAIL_PASSWORD="YOUR-GMAIL-APP-PASSWORD" `
  GOOGLE_CLIENT_ID="YOUR-CLIENT-ID" `
  GOOGLE_CLIENT_SECRET="YOUR-CLIENT-SECRET" `
  IMAGEKIT_PUBLIC_KEY="YOUR-PUBLIC-KEY" `
  IMAGEKIT_PRIVATE_KEY="YOUR-PRIVATE-KEY" `
  IMAGEKIT_URL_ENDPOINT="https://ik.imagekit.io/rx5x7e5fu" `
  GOOGLE_CALENDAR_ENABLED="false" `
  SESSION_TIMEOUT="1800"
```

---

## Troubleshooting

If health is still RED after setting environment variables:

1. Check logs:
   ```powershell
   eb logs
   ```

2. Look for database connection errors

3. Verify security group allows backend to connect to RDS:
   - RDS security group should allow port 5432 from backend security group

---

## Need Help?

Let me know:
1. Do you have an RDS database created?
2. What is the RDS endpoint?
3. Do you have all the credentials?

I can help you set everything up step by step.
