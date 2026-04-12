# DynamoDB Session Migration - AWS Deployment Update

## Overview

The AWS deployment specification has been updated to use **DynamoDB for session storage** instead of ElastiCache Redis. This change achieves **$0 deployment cost** by eliminating the ~$12-15/month ElastiCache expense while maintaining production-ready session management.

## Changes Summary

### Code Changes (Completed)

1. **New Configuration Files:**
   - `DynamoDBSessionConfig.java` - Production session configuration with DynamoDB
   - `DynamoDBSessionRepository.java` - Custom session repository implementation
   - `RedisSessionConfig.java` - Local development session configuration with Redis

2. **Updated Files:**
   - `pom.xml` - Added `spring-session-data-redis` dependency
   - `application.properties` - Added profile support and DynamoDB configuration
   - `.env` - Added `SPRING_PROFILES_ACTIVE` variable
   - `.env.example` - Added AWS and profile configuration examples

3. **Documentation:**
   - `SESSION_CONFIGURATION.md` - Comprehensive guide for both Redis and DynamoDB

### Deployment Spec Changes (Required)

The following spec files need to be updated to replace ElastiCache Redis with DynamoDB:

1. **requirements.md** - Update Requirement 5 (Cache Service) to use DynamoDB
2. **design.md** - Replace ElastiCache sections with DynamoDB configuration
3. **tasks.md** - Replace ElastiCache provisioning tasks with DynamoDB table creation

## Architecture Changes

### Before (ElastiCache Redis)
```
Backend API → ElastiCache Redis (cache.t3.micro)
Cost: ~$12-15/month
Free Tier: Not available
```

### After (DynamoDB)
```
Backend API → DynamoDB (spring-sessions table)
Cost: $0 (within free tier)
Free Tier: 25GB storage, 25 WCU, 25 RCU
```

## DynamoDB Table Specification

**Table Name:** `spring-sessions`

**Primary Key:**
- Partition Key: `sessionId` (String)

**Attributes:**
- `sessionId` (String) - Session identifier
- `creationTime` (Number) - Session creation timestamp (epoch milliseconds)
- `lastAccessedTime` (Number) - Last access timestamp (epoch milliseconds)
- `maxInactiveInterval` (Number) - Session timeout in seconds
- `expirationTime` (Number) - TTL attribute for automatic cleanup (epoch seconds)
- `attributes` (Map) - Session attributes (user data, etc.)

**Settings:**
- Billing Mode: On-Demand (or Provisioned with 5 RCU, 5 WCU)
- TTL: Enabled on `expirationTime` attribute
- Encryption: Enabled (default AWS managed key)
- Point-in-time Recovery: Optional (adds cost)

**IAM Permissions Required:**
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
    }
  ]
}
```

## Profile Configuration

### Local Development (dev profile)
- Uses Redis on `localhost:6379`
- No AWS credentials required
- Fast session access
- Ideal for development and testing

**Start application:**
```bash
mvn spring-boot:run
# or
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production (prod profile)
- Uses DynamoDB `spring-sessions` table
- Requires AWS credentials or IAM role
- Free tier eligible
- Auto-scaling and high availability

**Start application:**
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# or
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Deployment Steps

### 1. Create DynamoDB Table

**Via AWS CLI:**
```bash
# Create table
aws dynamodb create-table \
  --table-name spring-sessions \
  --attribute-definitions AttributeName=sessionId,AttributeType=S \
  --key-schema AttributeName=sessionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1

# Enable TTL
aws dynamodb update-time-to-live \
  --table-name spring-sessions \
  --time-to-live-specification Enabled=true,AttributeName=expirationTime \
  --region us-east-1
```

**Via AWS Console:**
1. Navigate to DynamoDB → Tables → Create table
2. Table name: `spring-sessions`
3. Partition key: `sessionId` (String)
4. Table settings: On-demand
5. Create table
6. Go to table → Additional settings → Time to Live
7. Enable TTL with attribute: `expirationTime`

### 2. Update IAM Role

Add DynamoDB permissions to the Elastic Beanstalk EC2 instance role:

```bash
# Create policy document
cat > dynamodb-session-policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:DeleteItem",
        "dynamodb:UpdateItem"
      ],
      "Resource": "arn:aws:dynamodb:us-east-1:*:table/spring-sessions"
    }
  ]
}
EOF

# Attach policy to role
aws iam put-role-policy \
  --role-name aws-elasticbeanstalk-ec2-role \
  --policy-name DynamoDBSessionAccess \
  --policy-document file://dynamodb-session-policy.json
```

### 3. Update Environment Variables

Set the production profile in Elastic Beanstalk:

```bash
# Via EB CLI
eb setenv SPRING_PROFILES_ACTIVE=prod AWS_REGION=us-east-1

# Via AWS CLI
aws elasticbeanstalk update-environment \
  --environment-name taskmanagement-backend-prod \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=SPRING_PROFILES_ACTIVE,Value=prod \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=AWS_REGION,Value=us-east-1
```

### 4. Deploy Application

```bash
# Rebuild application
cd Backend_Java/backend/backend
mvn clean package -DskipTests

# Deploy to Elastic Beanstalk
eb deploy

# Verify deployment
eb health
eb logs
```

### 5. Verify Session Storage

**Check DynamoDB table:**
```bash
# Scan table for sessions
aws dynamodb scan --table-name spring-sessions --limit 10

# Get specific session
aws dynamodb get-item \
  --table-name spring-sessions \
  --key '{"sessionId":{"S":"<session-id>"}}'
```

**Check application logs:**
```bash
eb logs | grep -i "dynamodb\|session"
```

## Cost Comparison

### Before (with ElastiCache Redis)
| Service | Instance Type | Monthly Cost |
|---------|--------------|--------------|
| EC2 (Backend) | t3.micro | $0 (free tier) |
| RDS PostgreSQL | db.t3.micro | $0 (free tier) |
| ElastiCache Redis | cache.t3.micro | ~$12-15 |
| **Total** | | **~$12-15/month** |

### After (with DynamoDB)
| Service | Instance Type | Monthly Cost |
|---------|--------------|--------------|
| EC2 (Backend) | t3.micro | $0 (free tier) |
| RDS PostgreSQL | db.t3.micro | $0 (free tier) |
| DynamoDB | On-demand | $0 (free tier) |
| **Total** | | **$0/month** |

**DynamoDB Free Tier:**
- 25 GB storage
- 25 WCU (Write Capacity Units)
- 25 RCU (Read Capacity Units)
- Sufficient for ~10,000 sessions with typical usage

## Migration from Existing Deployment

If you already have a deployment with ElastiCache Redis:

### Option 1: Zero-Downtime Migration (Recommended)

1. Create DynamoDB table
2. Update IAM role with DynamoDB permissions
3. Deploy new application version with both Redis and DynamoDB support
4. Switch profile from `dev` to `prod` via environment variable
5. Restart application
6. Verify sessions are being stored in DynamoDB
7. Delete ElastiCache cluster after 24 hours

### Option 2: Clean Migration (Requires Downtime)

1. Notify users of maintenance window
2. Create DynamoDB table
3. Update IAM role
4. Delete ElastiCache cluster
5. Update environment variables to use `prod` profile
6. Deploy new application version
7. Verify deployment
8. Users will need to log in again

## Troubleshooting

### Issue: Application can't connect to DynamoDB

**Symptoms:**
```
AmazonDynamoDBException: User is not authorized to perform: dynamodb:PutItem
```

**Solution:**
1. Verify IAM role has DynamoDB permissions
2. Check table name is `spring-sessions`
3. Verify AWS region matches table region
4. Check instance profile is attached to EC2 instances

### Issue: Sessions not persisting

**Symptoms:**
- Users logged out after each request
- Session data lost

**Solution:**
1. Verify `SPRING_PROFILES_ACTIVE=prod` is set
2. Check DynamoDB table exists
3. Verify application logs for errors
4. Check TTL is not too short

### Issue: High DynamoDB costs

**Symptoms:**
- DynamoDB charges appearing in AWS bill

**Solution:**
1. Check if you exceeded free tier limits
2. Review table's read/write capacity mode
3. Enable TTL to auto-delete expired sessions
4. Consider switching to provisioned capacity (5 RCU, 5 WCU)

## Testing

### Local Testing with DynamoDB

1. Install AWS CLI and configure credentials
2. Create DynamoDB table in your AWS account
3. Run application with prod profile:
```bash
export SPRING_PROFILES_ACTIVE=prod
export AWS_REGION=us-east-1
mvn spring-boot:run
```
4. Test login and session persistence
5. Verify sessions in DynamoDB console

### Production Testing

1. Deploy to Elastic Beanstalk
2. Test user login
3. Verify session persists across requests
4. Check DynamoDB table for session entries
5. Test session expiration (wait 30 minutes)
6. Verify expired sessions are deleted by TTL

## Rollback Plan

If issues occur with DynamoDB:

1. **Immediate rollback to Redis:**
```bash
# Recreate ElastiCache cluster (takes ~5 minutes)
aws elasticache create-cache-cluster \
  --cache-cluster-id taskmanagement-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1

# Switch back to dev profile
eb setenv SPRING_PROFILES_ACTIVE=dev REDIS_HOST=<redis-endpoint>

# Restart application
eb restart
```

2. **Users will need to log in again** (sessions are not migrated)

## Next Steps

1. ✅ Code implementation complete
2. ⏳ Update deployment spec files (requirements.md, design.md, tasks.md)
3. ⏳ Test locally with DynamoDB
4. ⏳ Deploy to AWS and verify
5. ⏳ Monitor DynamoDB usage and costs
6. ⏳ Update documentation with actual deployment experience

## References

- [Spring Session Documentation](https://docs.spring.io/spring-session/reference/)
- [AWS DynamoDB Free Tier](https://aws.amazon.com/dynamodb/pricing/)
- [DynamoDB TTL](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/TTL.html)
- [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
