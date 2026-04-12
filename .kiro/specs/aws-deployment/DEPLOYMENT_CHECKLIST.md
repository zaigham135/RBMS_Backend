# ✅ AWS Deployment Checklist

Use this checklist to track your progress. Check off each item as you complete it.

---

## 📋 Pre-Deployment Checklist

- [ ] AWS account created and verified
- [ ] Logged into AWS Console (https://console.aws.amazon.com/)
- [ ] AWS CLI installed on your computer
- [ ] EB CLI installed on your computer
- [ ] AWS credentials configured (`aws configure`)
- [ ] Application builds successfully locally
- [ ] All credentials ready (Gmail, Google OAuth, ImageKit)

---

## 🗄️ Step 1: DynamoDB Table (5 minutes)

- [ ] Navigate to DynamoDB service
- [ ] Create table named `spring-sessions`
- [ ] Partition key: `sessionId` (String)
- [ ] Table class: DynamoDB Standard
- [ ] Capacity mode: On-demand
- [ ] Table status shows "Active"
- [ ] Enable TTL on `expirationTime` attribute
- [ ] TTL status shows "Enabled"

**✅ Checkpoint:** Table exists and TTL is enabled

---

## 🌐 Step 2: VPC and Network (10 minutes)

- [ ] Navigate to VPC service
- [ ] Create VPC with "VPC and more" option
- [ ] Name: `taskmanagement`
- [ ] CIDR: `10.0.0.0/16`
- [ ] Availability Zones: 2
- [ ] Public subnets: 2
- [ ] Private subnets: 2
- [ ] NAT gateways: None
- [ ] DNS hostnames: Enabled
- [ ] VPC status shows "Available"
- [ ] **Write down VPC ID:** `vpc-________________`

**✅ Checkpoint:** VPC created with 2 public and 2 private subnets

---

## 🔒 Step 3: Security Groups (10 minutes)

### Backend Security Group
- [ ] Navigate to Security Groups
- [ ] Create security group: `taskmanagement-backend-sg`
- [ ] VPC: Select your taskmanagement VPC
- [ ] Inbound rule: Custom TCP, Port 5000, Source: 0.0.0.0/0
- [ ] Outbound rule: All traffic (default)
- [ ] **Write down SG ID:** `sg-________________`

### RDS Security Group
- [ ] Create security group: `taskmanagement-rds-sg`
- [ ] VPC: Select your taskmanagement VPC
- [ ] Inbound rule: PostgreSQL (5432), Source: Backend SG ID
- [ ] Outbound rules: Delete all (no outbound)
- [ ] **Write down SG ID:** `sg-________________`

**✅ Checkpoint:** 2 security groups created

---

## 🗃️ Step 4: RDS PostgreSQL Database (15 minutes)

- [ ] Navigate to RDS service
- [ ] Create database with Standard create
- [ ] Engine: PostgreSQL (latest version)
- [ ] Template: Free tier
- [ ] DB instance identifier: `taskmanagement-db`
- [ ] Master username: `postgres`
- [ ] Master password: (create strong password)
- [ ] **Write down password:** `________________`
- [ ] Instance class: db.t3.micro
- [ ] Storage: 20 GiB, gp2
- [ ] Storage autoscaling: Disabled
- [ ] VPC: Select your taskmanagement VPC
- [ ] Public access: No
- [ ] Security group: taskmanagement-rds-sg
- [ ] Initial database name: `taskmanagement`
- [ ] Backup retention: 7 days
- [ ] Encryption: Enabled
- [ ] Enhanced monitoring: Disabled
- [ ] Deletion protection: Disabled (for testing)
- [ ] Wait for status: "Available" (5-10 minutes)
- [ ] **Write down endpoint:** `________________.rds.amazonaws.com`

**✅ Checkpoint:** Database is available and endpoint is noted

---

## 🔐 Step 5: Parameter Store (15 minutes)

Navigate to Systems Manager → Parameter Store

### Database Parameters
- [ ] `/taskmanagement/prod/db/url` (String)
  - Value: `jdbc:postgresql://YOUR-ENDPOINT:5432/taskmanagement`
- [ ] `/taskmanagement/prod/db/username` (String)
  - Value: `postgres`
- [ ] `/taskmanagement/prod/db/password` (SecureString)
  - Value: Your database password

### AWS Parameters
- [ ] `/taskmanagement/prod/aws/region` (String)
  - Value: `us-east-1`

### Email Parameters
- [ ] `/taskmanagement/prod/mail/username` (String)
  - Value: Your Gmail address
- [ ] `/taskmanagement/prod/mail/password` (SecureString)
  - Value: Your Gmail App Password

### Google OAuth Parameters
- [ ] `/taskmanagement/prod/google/client-id` (String)
  - Value: Your OAuth Client ID
- [ ] `/taskmanagement/prod/google/client-secret` (SecureString)
  - Value: Your OAuth Client Secret

### ImageKit Parameters
- [ ] `/taskmanagement/prod/imagekit/public-key` (String)
  - Value: Your ImageKit public key
- [ ] `/taskmanagement/prod/imagekit/private-key` (SecureString)
  - Value: Your ImageKit private key
- [ ] `/taskmanagement/prod/imagekit/url-endpoint` (String)
  - Value: `https://ik.imagekit.io/rx5x7e5fu`

**✅ Checkpoint:** All 11 parameters created

---

## 👤 Step 6: IAM Role (10 minutes)

- [ ] Navigate to IAM service
- [ ] Create role for EC2 service
- [ ] Attach policy: AWSElasticBeanstalkWebTier
- [ ] Attach policy: AWSElasticBeanstalkMulticontainerDocker
- [ ] Attach policy: AWSElasticBeanstalkWorkerTier
- [ ] Role name: `taskmanagement-eb-ec2-role`
- [ ] Create inline policy for DynamoDB and Parameter Store
- [ ] Policy name: `DynamoDBAndParameterStoreAccess`
- [ ] Role created successfully

**✅ Checkpoint:** IAM role has all required permissions

---

## 🚀 Step 7: Deploy Backend (20 minutes)

### Local Setup
- [ ] AWS CLI configured (`aws configure`)
- [ ] EB CLI installed (`pip install awsebcli`)
- [ ] Navigate to backend directory
- [ ] Create `.ebextensions` folder
- [ ] Create `01_java.config` file
- [ ] Build application: `mvn clean package -DskipTests`
- [ ] JAR file created successfully

### Elastic Beanstalk
- [ ] Initialize EB: `eb init`
- [ ] Application name: `taskmanagement-backend`
- [ ] Platform: Java 17 (Corretto)
- [ ] Create environment: `eb create taskmanagement-backend-prod`
- [ ] Instance type: t3.micro
- [ ] Instance profile: taskmanagement-eb-ec2-role
- [ ] Wait for environment creation (5-10 minutes)
- [ ] Set environment variables with `eb setenv`
- [ ] Deploy application: `eb deploy`
- [ ] Check status: `eb status`
- [ ] Environment health: Green
- [ ] **Write down backend URL:** `________________.elasticbeanstalk.com`

### Verification
- [ ] Test health endpoint: `https://YOUR-BACKEND-URL/actuator/health`
- [ ] Response: `{"status":"UP"}`

**✅ Checkpoint:** Backend is deployed and healthy

---

## 🎨 Step 8: Deploy Frontend (20 minutes)

### Local Setup
- [ ] Navigate to frontend directory
- [ ] Create `.ebextensions` folder
- [ ] Create `01_nodecommands.config` file
- [ ] Update `NEXT_PUBLIC_API_URL` with backend URL
- [ ] Install dependencies: `npm install`
- [ ] Build application: `npm run build`
- [ ] Build completed successfully

### Elastic Beanstalk
- [ ] Initialize EB: `eb init`
- [ ] Application name: `taskmanagement-frontend`
- [ ] Platform: Node.js 20
- [ ] Create environment: `eb create taskmanagement-frontend-prod`
- [ ] Instance type: t3.micro
- [ ] Wait for environment creation (5-10 minutes)
- [ ] Deploy application: `eb deploy`
- [ ] Check status: `eb status`
- [ ] Environment health: Green
- [ ] **Write down frontend URL:** `________________.elasticbeanstalk.com`

### Verification
- [ ] Open frontend URL in browser
- [ ] Application loads successfully
- [ ] Login page appears

**✅ Checkpoint:** Frontend is deployed and accessible

---

## 🔒 Step 9: Configure HTTPS (Optional, 10 minutes)

- [ ] Navigate to Elastic Beanstalk → Frontend environment
- [ ] Go to Configuration → Load balancer
- [ ] Add HTTPS listener on port 443
- [ ] Select or create SSL certificate
- [ ] Apply changes
- [ ] Wait for update to complete
- [ ] Test HTTPS: `https://YOUR-FRONTEND-URL`

**✅ Checkpoint:** HTTPS is working

---

## ✅ Step 10: Final Verification (10 minutes)

### Backend Tests
- [ ] Health check returns UP: `/actuator/health`
- [ ] Check logs: `eb logs` (no errors)
- [ ] Database connection working (check logs)

### Frontend Tests
- [ ] Application loads in browser
- [ ] Login page displays correctly
- [ ] Can create account
- [ ] Can log in
- [ ] Dashboard loads after login

### DynamoDB Tests
- [ ] Navigate to DynamoDB → spring-sessions table
- [ ] Click "Explore table items"
- [ ] Session data appears after login
- [ ] Session has `sessionId`, `creationTime`, `expirationTime`

### Integration Tests
- [ ] Login works end-to-end
- [ ] Session persists across page refreshes
- [ ] Can create tasks/projects
- [ ] Email notifications work
- [ ] Profile photo upload works (ImageKit)

**✅ Checkpoint:** Everything is working!

---

## 📊 Cost Verification

- [ ] Navigate to AWS Billing Dashboard
- [ ] Check current month charges
- [ ] Verify within free tier limits:
  - EC2: 750 hours/month (t3.micro)
  - RDS: 750 hours/month (db.t3.micro)
  - DynamoDB: 25GB storage, 25 WCU, 25 RCU
- [ ] Set up billing alerts (optional)

**Expected Cost: $0/month** (within free tier)

---

## 🎉 Deployment Complete!

Congratulations! You've successfully deployed your application to AWS.

### What You've Accomplished:
✅ DynamoDB session storage ($0 cost)
✅ RDS PostgreSQL database ($0 with free tier)
✅ Elastic Beanstalk hosting ($0 with free tier)
✅ Secure secrets management
✅ Production-ready infrastructure
✅ HTTPS enabled (optional)

### Next Steps:
- [ ] Set up monitoring and alerts
- [ ] Configure custom domain (optional)
- [ ] Set up CI/CD pipeline (optional)
- [ ] Enable CloudWatch logs
- [ ] Create backup strategy
- [ ] Document your deployment

---

## 📝 Important Information to Save

**VPC ID:** `vpc-________________`

**Security Group IDs:**
- Backend: `sg-________________`
- RDS: `sg-________________`

**Database:**
- Endpoint: `________________.rds.amazonaws.com`
- Password: `________________` (keep secure!)

**Application URLs:**
- Backend: `________________.elasticbeanstalk.com`
- Frontend: `________________.elasticbeanstalk.com`

**IAM Role:** `taskmanagement-eb-ec2-role`

**DynamoDB Table:** `spring-sessions`

---

## 🆘 Troubleshooting

If something doesn't work:

1. **Check logs:**
   ```bash
   eb logs
   ```

2. **Check environment health:**
   ```bash
   eb health
   ```

3. **Verify parameters:**
   - AWS Console → Systems Manager → Parameter Store
   - Ensure all 11 parameters exist

4. **Check security groups:**
   - Backend SG allows port 5000
   - RDS SG allows port 5432 from Backend SG

5. **Verify IAM role:**
   - Role has DynamoDB permissions
   - Role has Parameter Store permissions

6. **Check DynamoDB:**
   - Table exists
   - TTL is enabled
   - IAM role can access table

---

## 📞 Need Help?

- AWS Documentation: https://docs.aws.amazon.com/
- Elastic Beanstalk Guide: https://docs.aws.amazon.com/elasticbeanstalk/
- DynamoDB Guide: https://docs.aws.amazon.com/dynamodb/
- Spring Session: https://docs.spring.io/spring-session/

---

**Last Updated:** 2024
**Estimated Total Time:** 2-3 hours
**Estimated Cost:** $0/month (within free tier)
