# Fix Security Group / VPC Configuration

## Problem
The application cannot connect to RDS because:
- Elastic Beanstalk created its own VPC and security groups
- RDS is in your `taskmanagement-vpc` with `taskmanagement-rds-sg`
- They cannot communicate across different VPCs/security groups

## Solution: Configure EB to use your existing VPC and security groups

---

## Step 1: Go to Elastic Beanstalk Configuration

1. Open AWS Console → Elastic Beanstalk
2. **Make sure region is set to eu-north-1** (top right)
3. Click on `taskmanagement-backend-prod` environment
4. Click **Configuration** in the left sidebar

---

## Step 2: Update Network Configuration

1. Find the **Network** card
2. Click **Edit**

**VPC Settings:**
- **VPC**: Select `taskmanagement-vpc` (the one you created for RDS)

**Load balancer settings:**
- **Load balancer subnets**: Check the **PUBLIC** subnets (should be 2)
  - Look for subnets with names like `taskmanagement-subnet-public1-eu-north-1a`
  - Look for subnets with names like `taskmanagement-subnet-public1-eu-north-1b`

**Instance settings:**
- **Instance subnets**: Check the **PRIVATE** subnets (should be 2)
  - Look for subnets with names like `taskmanagement-subnet-private1-eu-north-1a`
  - Look for subnets with names like `taskmanagement-subnet-private1-eu-north-1b`
  - **OR** if you don't have private subnets, use the same PUBLIC subnets

**Security groups:**
- **Instance security groups**: Select `taskmanagement-backend-sg`
  - If this doesn't exist, we'll create it in Step 3

3. Click **Apply** at the bottom
4. Wait 3-5 minutes for the environment to update

---

## Step 3: Create/Update Security Groups (if needed)

### Check if taskmanagement-backend-sg exists:

1. Go to EC2 → Security Groups
2. **Make sure region is eu-north-1**
3. Search for `taskmanagement-backend-sg`

### If it doesn't exist, create it:

1. Click **Create security group**
2. **Security group name**: `taskmanagement-backend-sg`
3. **Description**: Security group for backend application
4. **VPC**: Select `taskmanagement-vpc`

**Inbound rules:**
- Type: HTTP, Port: 80, Source: 0.0.0.0/0
- Type: Custom TCP, Port: 5000, Source: 0.0.0.0/0

**Outbound rules:**
- Type: All traffic, Destination: 0.0.0.0/0 (default)

5. Click **Create security group**
6. **Copy the security group ID** (e.g., sg-xxxxx)

### Update RDS Security Group:

1. Go to EC2 → Security Groups
2. Find `taskmanagement-rds-sg`
3. Click on it → **Inbound rules** tab → **Edit inbound rules**
4. **Add rule**:
   - Type: PostgreSQL
   - Port: 5432
   - Source: Custom → Select `taskmanagement-backend-sg` (or paste the sg-xxxxx ID)
   - Description: Allow backend to connect to RDS
5. Click **Save rules**

---

## Step 4: Verify Configuration

After the environment update completes:

1. Check health:
   ```powershell
   eb health
   ```

2. If still Red, check logs:
   ```powershell
   eb logs
   ```

3. Test the health endpoint:
   ```powershell
   $url = (eb status | Select-String "CNAME:" | ForEach-Object { $_.ToString().Split(":")[1].Trim() })
   curl "http://$url/actuator/health"
   ```

Expected: `{"status":"UP"}`

---

## Alternative: If VPC Configuration Fails

If you can't configure the VPC through EB Console, you can:

1. **Make RDS publicly accessible** (temporary, for testing):
   - Go to RDS → Databases → taskmanagement-db
   - Click **Modify**
   - **Connectivity** → **Public access**: Yes
   - Click **Continue** → **Apply immediately**
   - Wait for modification to complete
   
2. **Update RDS security group** to allow connections from anywhere:
   - Go to EC2 → Security Groups → taskmanagement-rds-sg
   - Edit inbound rules
   - Add rule: PostgreSQL (5432), Source: 0.0.0.0/0
   - **WARNING**: This is insecure! Only for testing.

3. **Test again**:
   ```powershell
   eb health
   ```

---

## Quick Checklist

- [ ] EB environment configured to use taskmanagement-vpc
- [ ] EB instance subnets selected (private or public)
- [ ] EB load balancer subnets selected (public)
- [ ] taskmanagement-backend-sg created
- [ ] EB configured to use taskmanagement-backend-sg
- [ ] RDS security group allows connections from backend SG
- [ ] Environment health is Green
- [ ] Health endpoint returns UP

---

## Need Help?

The key issue is that EB and RDS must be in the same VPC and the security groups must allow communication between them.

Let me know if you need help with any of these steps!
