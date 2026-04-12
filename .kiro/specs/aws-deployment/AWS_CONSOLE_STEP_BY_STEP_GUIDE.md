# 🎯 AWS Console Step-by-Step Guide for Complete Beginners

This guide assumes you only have an AWS account and are logged into the AWS Console. I'll tell you exactly where to click and what to type.

---

## 📋 Table of Contents

1. [Create DynamoDB Table for Sessions](#step-1-create-dynamodb-table)
2. [Create VPC and Network](#step-2-create-vpc-and-network)
3. [Create Security Groups](#step-3-create-security-groups)
4. [Create RDS PostgreSQL Database](#step-4-create-rds-database)
5. [Store Secrets in Parameter Store](#step-5-store-secrets)
6. [Create IAM Role for Elastic Beanstalk](#step-6-create-iam-role)
7. [Deploy Backend Application](#step-7-deploy-backend)
8. [Deploy Frontend Application](#step-8-deploy-frontend)
9. [Configure HTTPS](#step-9-configure-https)
10. [Verify Deployment](#step-10-verify-deployment)

---

## Step 1: Create DynamoDB Table for Sessions

### 1.1 Navigate to DynamoDB

1. **Log in to AWS Console**: https://console.aws.amazon.com/
2. At the top, in the search bar, type: `DynamoDB`
3. Click on **DynamoDB** (it will say "Fast, flexible NoSQL database")

### 1.2 Create Table

1. Click the orange **"Create table"** button (top right)

2. **Fill in the form:**
   - **Table name**: Type `spring-sessions`
   - **Partition key**: Type `sessionId`
   - **Partition key type**: Leave as `String` (default)
   - **Sort key**: Leave UNCHECKED (don't add a sort key)

3. **Table settings:**
   - Scroll down to "Table settings"
   - Select **"Customize settings"** (not "Default settings")

4. **Table class:**
   - Leave as **"DynamoDB Standard"** (default)

5. **Read/write capacity settings:**
   - Select **"On-demand"** (this is free tier eligible)
   - Do NOT select "Provisioned"

6. **Scroll down and click the orange "Create table" button at the bottom**

7. **Wait for table creation** (takes 10-30 seconds)
   - You'll see "Creating..." status
   - Wait until it says "Active" (green)

### 1.3 Enable Time To Live (TTL)

1. Click on your **"spring-sessions"** table name (in the list)

2. Click the **"Additional settings"** tab (top menu)

3. Scroll down to **"Time to Live (TTL)"** section

4. Click the **"Edit"** button

5. **Enable TTL:**
   - Toggle **"Turn on TTL"** to ON (blue)
   - **Attribute name**: Type `expirationTime`
   - Click **"Save changes"** button

✅ **DynamoDB table is ready!**

---

## Step 2: Create VPC and Network

### 2.1 Navigate to VPC

1. In the search bar at top, type: `VPC`
2. Click on **VPC** (Virtual Private Cloud)

### 2.2 Create VPC

1. Click **"Create VPC"** button (orange, top right)

2. **VPC settings:**
   - Select **"VPC and more"** (this creates everything automatically)
   - **Name tag auto-generation**: Type `taskmanagement`
   - **IPv4 CIDR block**: Type `10.0.0.0/16`
   - **IPv6 CIDR block**: Select "No IPv6 CIDR block"
   - **Tenancy**: Leave as "Default"

3. **Number of Availability Zones (AZs):**
   - Select **2**

4. **Number of public subnets:**
   - Select **2**

5. **Number of private subnets:**
   - Select **2**

6. **NAT gateways:**
   - Select **"None"** (to save costs - $0.045/hour = ~$32/month)

7. **VPC endpoints:**
   - Select **"None"**

8. **DNS options:**
   - Check ✅ **"Enable DNS hostnames"**
   - Check ✅ **"Enable DNS resolution"**

9. Click **"Create VPC"** button (orange, bottom right)

10. **Wait for creation** (takes 1-2 minutes)
    - You'll see a progress screen
    - Wait until all items show green checkmarks

✅ **VPC and subnets are ready!**

### 2.3 Note Your VPC ID

1. After creation, you'll see a success message  vpc-0edfc3ed09bc62a1e
2. **Write down your VPC ID** (looks like: `vpc-0123456789abcdef0`)
3. You'll need this later!

---

## Step 3: Create Security Groups

### 3.1 Navigate to Security Groups

1. In the left sidebar, click **"Security Groups"** (under "Security" section)
2. Click **"Create security group"** button (orange, top right)

### 3.2 Create Backend Security Group

1. **Basic details:**
   - **Security group name**: Type `taskmanagement-backend-sg`
   - **Description**: Type `Security group for backend API`
   - **VPC**: Select your VPC (starts with `taskmanagement-vpc`)

2. **Inbound rules:**
   - Click **"Add rule"** button
   - **Type**: Select `Custom TCP`
   - **Port range**: Type `5000`
   - **Source**: Select `Anywhere-IPv4` (0.0.0.0/0) - we'll restrict this later
   - **Description**: Type `Backend API port`

3. **Outbound rules:**
   - Leave default (All traffic to 0.0.0.0/0)

4. Click **"Create security group"** button (orange, bottom right)
sgr-0d2ba8b12c72ecf4f
5. **Write down the Security Group ID** (looks like: `sg-0123456789abcdef0`)

### 3.3 Create RDS Security Group

1. Click **"Create security group"** button again

2. **Basic details:**
   - **Security group name**: Type `taskmanagement-rds-sg`
   - **Description**: Type `Security group for RDS PostgreSQL`
   - **VPC**: Select your VPC (starts with `taskmanagement-vpc`)

3. **Inbound rules:**
   - Click **"Add rule"** button
   - **Type**: Select `PostgreSQL` (it will auto-fill port 5432)
   - **Source**: Select `Custom`
   - In the search box that appears, paste your **backend security group ID** (from step 3.2)
   - **Description**: Type `Allow from backend`

4. **Outbound rules:**
   - Click **"Delete"** on the default rule (we don't want any outbound)

5. Click **"Create security group"** button

6. **Write down the Security Group ID**
sg-011a869dffb917c60
✅ **Security groups are ready!**

---

## Step 4: Create RDS PostgreSQL Database

### 4.1 Navigate to RDS

1. In the search bar at top, type: `RDS`
2. Click on **RDS** (Relational Database Service)

### 4.2 Create Database

1. Click **"Create database"** button (orange, top right)

2. **Choose a database creation method:**
   - Select **"Standard create"**

3. **Engine options:**
   - Select **"PostgreSQL"**
   - **Engine Version**: Select the latest version (e.g., `PostgreSQL 14.10-R2`)

4. **Templates:**
   - Select **"Free tier"** (this is important for $0 cost!)

5. **Settings:**
   - **DB instance identifier**: Type `taskmanagement-db`
   - **Master username**: Type `postgres`
   - **Master password**: Type a secure password (e.g., `TaskMgmt2024!`)
   - **Confirm password**: Type the same password again
   - **⚠️ IMPORTANT: Write down this password!**
TaskMgmt2024!
6. **Instance configuration:**
   - **DB instance class**: Should be `db.t3.micro` (auto-selected with Free tier)

7. **Storage:**
   - **Storage type**: `General Purpose SSD (gp2)`
   - **Allocated storage**: `20` GiB (default)
   - **Storage autoscaling**: Uncheck ❌ "Enable storage autoscaling"

8. **Connectivity:**
   - **Compute resource**: Select "Don't connect to an EC2 compute resource"
   - **Virtual private cloud (VPC)**: Select your VPC (starts with `taskmanagement-vpc`)
   - **DB subnet group**: Click "Create new DB Subnet Group"
   - **Public access**: Select **"No"** (important for security!)
   - **VPC security group**: Select "Choose existing"
   - Remove the default security group
   - Select your **RDS security group** (taskmanagement-rds-sg)
   - **Availability Zone**: Select "No preference"

9. **Database authentication:**
   - Select **"Password authentication"**

10. **Additional configuration** (click to expand):
    - **Initial database name**: Type `taskmanagement`
    - **DB parameter group**: Leave default
    - **Option group**: Leave default
    - **Backup**:
      - **Backup retention period**: `7 days`
      - **Backup window**: Select "No preference"
    - **Encryption**: Check ✅ "Enable encryption"
    - **Monitoring**: Uncheck ❌ "Enable Enhanced monitoring" (to save costs)
    - **Maintenance**:
      - Check ✅ "Enable auto minor version upgrade"
      - **Maintenance window**: Select "No preference"
    - **Deletion protection**: Uncheck ❌ (for easier cleanup during testing)

11. **Scroll down and click "Create database"** button (orange)

12. **Wait for database creation** (takes 5-10 minutes)
    - Status will show "Creating..."
    - Wait until status shows "Available" (green)
    - ☕ Take a coffee break!

### 4.3 Get Database Endpoint

1. Once status is "Available", click on your database name **"taskmanagement-db"**

2. Scroll down to **"Connectivity & security"** section

3. **Copy the Endpoint** (looks like: `taskmanagement-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com`)
curl -o global-bundle.pem https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem


export RDSHOST="taskmanagement-db.clmcow6yccmx.eu-north-1.rds.amazonaws.com" 
psql "host=$RDSHOST port=5432 dbname=taskmanagement user=postgres sslmode=verify-full sslrootcert=./global-bundle.pem"

export RDSHOST="taskmanagement-db.clmcow6yccmx.eu-north-1.rds.amazonaws.com" 
psql "host=$RDSHOST port=5432 dbname=taskmanagement user=postgres sslmode=verify-full sslrootcert=./global-bundle.pem"

4. **Write this down!** You'll need it for Parameter Store

✅ **Database is ready!**

---

## Step 5: Store Secrets in Parameter Store

### 5.1 Navigate to Systems Manager

1. In the search bar at top, type: `Systems Manager`
2. Click on **Systems Manager**

### 5.2 Go to Parameter Store

1. In the left sidebar, scroll down to **"Application Management"** section
2. Click **"Parameter Store"**

### 5.3 Create Database URL Parameter

1. Click **"Create parameter"** button (orange, top right)

2. **Parameter details:**
   - **Name**: Type `/taskmanagement/prod/db/url`
   - **Description**: Type `Database connection URL`
   - **Tier**: Select "Standard"
   - **Type**: Select "String"
   - **Data type**: Select "text"
   - **Value**: Type `jdbc:postgresql://YOUR-RDS-ENDPOINT:5432/taskmanagement`
     - Replace `YOUR-RDS-ENDPOINT` with the endpoint you copied in Step 4.3
     - Example: `jdbc:postgresql://taskmanagement-db.abc123.us-east-1.rds.amazonaws.com:5432/taskmanagement`

3. Click **"Create parameter"** button (orange, bottom)

### 5.4 Create Database Username Parameter

1. Click **"Create parameter"** button again

2. **Parameter details:**
   - **Name**: Type `/taskmanagement/prod/db/username`
   - **Description**: Type `Database username`
   - **Tier**: Select "Standard"
   - **Type**: Select "String"
   - **Data type**: Select "text"
   - **Value**: Type `postgres`

3. Click **"Create parameter"** button

### 5.5 Create Database Password Parameter (Secure)

1. Click **"Create parameter"** button again

2. **Parameter details:**
   - **Name**: Type `/taskmanagement/prod/db/password`
   - **Description**: Type `Database password`
   - **Tier**: Select "Standard"
   - **Type**: Select **"SecureString"** (important!)
   - **KMS key source**: Select "My current account"
   - **KMS Key ID**: Leave as `alias/aws/ssm` (default)
   - **Value**: Type your database password (from Step 4.2)

3. Click **"Create parameter"** button

### 5.6 Create AWS Region Parameter

1. Click **"Create parameter"** button again

2. **Parameter details:**
   - **Name**: Type `/taskmanagement/prod/aws/region`
   - **Description**: Type `AWS region`
   - **Tier**: Select "Standard"
   - **Type**: Select "String"
   - **Data type**: Select "text"
   - **Value**: Type `us-east-1` (or your region)

3. Click **"Create parameter"** button

### 5.7 Create Email Parameters

Repeat the "Create parameter" process for each of these:

**Mail Username:**
- **Name**: `/taskmanagement/prod/mail/username`
- **Type**: String
- **Value**: Your Gmail address (e.g., `your-email@gmail.com`)

**Mail Password (Secure):**
- **Name**: `/taskmanagement/prod/mail/password`
- **Type**: **SecureString**
- **Value**: Your Gmail App Password (not your regular password!)
  - To get Gmail App Password: https://myaccount.google.com/apppasswords

### 5.8 Create Google OAuth Parameters

**Google Client ID:**
- **Name**: `/taskmanagement/prod/google/client-id`
- **Type**: String
- **Value**: Your Google OAuth Client ID

**Google Client Secret (Secure):**
- **Name**: `/taskmanagement/prod/google/client-secret`
- **Type**: **SecureString**
- **Value**: Your Google OAuth Client Secret

### 5.9 Create ImageKit Parameters

**ImageKit Public Key:**
- **Name**: `/taskmanagement/prod/imagekit/public-key`
- **Type**: String
- **Value**: Your ImageKit public key

**ImageKit Private Key (Secure):**
- **Name**: `/taskmanagement/prod/imagekit/private-key`
- **Type**: **SecureString**
- **Value**: Your ImageKit private key

**ImageKit URL Endpoint:**
- **Name**: `/taskmanagement/prod/imagekit/url-endpoint`
- **Type**: String
- **Value**: `https://ik.imagekit.io/rx5x7e5fu`

✅ **All secrets are stored securely!**

---

## Step 6: Create IAM Role for Elastic Beanstalk

### 6.1 Navigate to IAM

1. In the search bar at top, type: `IAM`
2. Click on **IAM** (Identity and Access Management)

### 6.2 Create Role

1. In the left sidebar, click **"Roles"**
2. Click **"Create role"** button (orange, top right)

3. **Select trusted entity:**
   - Select **"AWS service"**
   - **Use case**: Select **"EC2"**
   - Click **"Next"** button

4. **Add permissions:**
   - In the search box, type: `AWSElasticBeanstalkWebTier`
   - Check ✅ **AWSElasticBeanstalkWebTier**
   - In the search box, type: `AWSElasticBeanstalkMulticontainerDocker`
   - Check ✅ **AWSElasticBeanstalkMulticontainerDocker**
   - In the search box, type: `AWSElasticBeanstalkWorkerTier`
   - Check ✅ **AWSElasticBeanstalkWorkerTier**
   - Click **"Next"** button

5. **Name, review, and create:**
   - **Role name**: Type `taskmanagement-eb-ec2-role`
   - **Description**: Type `IAM role for Elastic Beanstalk EC2 instances`
   - Click **"Create role"** button (orange, bottom)

### 6.3 Add DynamoDB and Parameter Store Permissions

1. In the roles list, click on your new role **"taskmanagement-eb-ec2-role"**

2. Click **"Add permissions"** dropdown button
3. Select **"Create inline policy"**

4. Click **"JSON"** tab

5. **Delete everything** in the editor and paste this:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:DeleteItem",
        "dynamodb:UpdateItem",
        "dynamodb:Query",
        "dynamodb:Scan"
      ],
      "Resource": "arn:aws:dynamodb:us-east-1:*:table/spring-sessions"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ssm:GetParameter",
        "ssm:GetParameters",
        "ssm:GetParametersByPath"
      ],
      "Resource": "arn:aws:ssm:us-east-1:*:parameter/taskmanagement/prod/*"
    }
  ]
}
```

6. Click **"Next"** button

7. **Policy name**: Type `DynamoDBAndParameterStoreAccess`

8. Click **"Create policy"** button

✅ **IAM role is ready!**

---

## Step 7: Deploy Backend Application

### 7.1 Install AWS CLI and EB CLI (One-time setup)

**On Windows:**

1. Download AWS CLI: https://awscli.amazonaws.com/AWSCLIV2.msi
2. Run the installer
3. Open PowerShell and verify: `aws --version`

4. Install EB CLI:
```powershell
pip install awsebcli
```

5. Verify: `eb --version`

### 7.2 Configure AWS CLI

1. Open PowerShell
2. Run: `aws configure`
3. Enter your AWS credentials:
   - **AWS Access Key ID**: Get from AWS Console → IAM → Users → Your User → Security credentials → Create access key
   - **AWS Secret Access Key**: Shown only once when creating access key
   - **Default region name**: Type `us-east-1`
   - **Default output format**: Type `json`

### 7.3 Prepare Backend Application

1. Open PowerShell
2. Navigate to backend directory:
```powershell
cd "E:\role based task  management project\Backend_Java\backend\backend"
```

3. Create `.ebextensions` directory:
```powershell
mkdir .ebextensions
```

4. Create configuration file:
```powershell
@"
option_settings:
  aws:elasticbeanstalk:application:environment:
    SERVER_PORT: 5000
    SPRING_PROFILES_ACTIVE: prod
    AWS_REGION: us-east-1
  aws:elasticbeanstalk:container:java:
    JVMOptions: "-Xmx512m -Xms256m"
"@ | Out-File -FilePath .ebextensions\01_java.config -Encoding UTF8
```

5. Build the application:
```powershell
mvn clean package -DskipTests
```

### 7.4 Initialize Elastic Beanstalk

1. Initialize EB:
```powershell
eb init
```

2. Answer the prompts:
   - **Select a default region**: Choose your region (e.g., `10) us-east-1`)
   - **Select an application to use**: Choose `[ Create new Application ]`
   - **Enter Application Name**: Type `taskmanagement-backend`
   - **It appears you are using Java. Is this correct?**: Type `y`
   - **Select a platform branch**: Choose `Corretto 17` (latest)
   - **Do you wish to continue with CodeCommit?**: Type `n`
   - **Do you want to set up SSH for your instances?**: Type `n`

### 7.5 Create Environment

1. Create environment:
```powershell
eb create taskmanagement-backend-prod --instance-type t3.micro --instance-profile taskmanagement-eb-ec2-role
```
'''''
eb create taskmanagement-backend-prod --instance-type t3.micro
''''

2. **Wait for environment creation** (takes 5-10 minutes)
   - You'll see progress messages
   - Wait for "Successfully launched environment"

### 7.6 Configure Environment Variables

1. Set environment variables:
```powershell
eb setenv `
  DB_URL="{{resolve:ssm:/taskmanagement/prod/db/url}}" `
  DB_USERNAME="{{resolve:ssm:/taskmanagement/prod/db/username}}" `
  DB_PASSWORD="{{resolve:ssm-secure:/taskmanagement/prod/db/password}}" `
  MAIL_USERNAME="{{resolve:ssm:/taskmanagement/prod/mail/username}}" `
  MAIL_PASSWORD="{{resolve:ssm-secure:/taskmanagement/prod/mail/password}}" `
  GOOGLE_CLIENT_ID="{{resolve:ssm:/taskmanagement/prod/google/client-id}}" `
  GOOGLE_CLIENT_SECRET="{{resolve:ssm-secure:/taskmanagement/prod/google/client-secret}}" `
  IMAGEKIT_PUBLIC_KEY="{{resolve:ssm:/taskmanagement/prod/imagekit/public-key}}" `
  IMAGEKIT_PRIVATE_KEY="{{resolve:ssm-secure:/taskmanagement/prod/imagekit/private-key}}" `
  IMAGEKIT_URL_ENDPOINT="{{resolve:ssm:/taskmanagement/prod/imagekit/url-endpoint}}"
```

### 7.7 Deploy Application

1. Deploy:
```powershell
eb deploy
```

2. **Wait for deployment** (takes 3-5 minutes)

3. Check status:
```powershell
eb status
```

4. Get URL:
```powershell
eb status | Select-String "CNAME"
```

5. **Write down the backend URL!**

✅ **Backend is deployed!**
taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com
---

## Step 8: Deploy Frontend Application

### 8.1 Prepare Frontend

1. Navigate to frontend directory:
```powershell
cd "E:\role based task  management project\Nextjs_Frontend"
```

2. Create `.ebextensions` directory:
```powershell
mkdir .ebextensions
```

3. Create configuration file (replace `YOUR-BACKEND-URL` with URL from Step 7.7):
```powershell
@"
option_settings:
  aws:elasticbeanstalk:container:nodejs:
    NodeCommand: "npm start"
    NodeVersion: 20.x
  aws:elasticbeanstalk:application:environment:
    NODE_ENV: production
    NEXT_PUBLIC_API_URL: taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com
"@ | Out-File -FilePath .ebextensions\01_nodecommands.config -Encoding UTF8
```

4. Build application:
```powershell
npm install
npm run build
```

### 8.2 Initialize and Deploy

1. Initialize EB:
```powershell
eb init
```

2. Answer prompts:
   - **Select a default region**: Same as backend
   - **Select an application**: Choose `[ Create new Application ]`
   - **Enter Application Name**: Type `taskmanagement-frontend`
   - **It appears you are using Node.js. Is this correct?**: Type `y`
   - **Select a platform branch**: Choose `Node.js 20`
   - **Do you wish to continue with CodeCommit?**: Type `n`
   - **Do you want to set up SSH?**: Type `n`

3. Create environment:
```powershell
eb create taskmanagement-frontend-prod --instance-type t3.micro
```

4. Deploy:
```powershell
eb deploy
```

5. Get URL:
```powershell
eb status | Select-String "CNAME"
```

✅ **Frontend is deployed!**

---

## Step 9: Configure HTTPS

### 9.1 Enable HTTPS in Elastic Beanstalk

1. Go to AWS Console → Elastic Beanstalk
2. Click on **"taskmanagement-frontend-prod"** environment
3. Click **"Configuration"** in left sidebar
4. Scroll to **"Load balancer"** section
5. Click **"Edit"** button
6. Scroll to **"Listeners"** section
7. Click **"Add listener"** button
8. **Listener port**: Type `443`
9. **Listener protocol**: Select `HTTPS`
10. **SSL certificate**: Select "Use an existing certificate" or create new one
11. Click **"Add"** button
12. Click **"Apply"** button at bottom

---

## Step 10: Verify Deployment

### 10.1 Test Backend

1. Open browser
2. Go to: `https://YOUR-BACKEND-URL/actuator/health`
3. You should see: `{"status":"UP"}`

### 10.2 Test Frontend

1. Go to: `https://YOUR-FRONTEND-URL`
2. You should see your application login page

### 10.3 Test DynamoDB Sessions

1. Log in to your application
2. Go to AWS Console → DynamoDB → Tables → spring-sessions
3. Click **"Explore table items"** button
4. You should see your session data!

✅ **Everything is working!**

---

## 🎉 Congratulations!

You've successfully deployed your application to AWS with:
- ✅ DynamoDB for sessions ($0 cost)
- ✅ RDS PostgreSQL database ($0 with free tier)
- ✅ Elastic Beanstalk for hosting ($0 with free tier)
- ✅ Secure secrets management
- ✅ HTTPS enabled

**Total Monthly Cost: $0** (within free tier limits)

---

## 📞 Need Help?

If you get stuck:
1. Check AWS CloudWatch Logs for errors
2. Run `eb logs` in PowerShell to see application logs
3. Verify all parameters in Parameter Store are correct
4. Check security group rules allow traffic
5. Ensure IAM role has all required permissions

---

## 🔄 Common Issues

### Issue: "Environment creation failed"
**Solution:** Check CloudWatch Logs in AWS Console → CloudWatch → Log groups

### Issue: "Application not responding"
**Solution:** Run `eb logs` to see what's wrong

### Issue: "Database connection failed"
**Solution:** Verify RDS security group allows traffic from backend security group

### Issue: "Sessions not persisting"
**Solution:** Check DynamoDB table exists and IAM role has permissions
