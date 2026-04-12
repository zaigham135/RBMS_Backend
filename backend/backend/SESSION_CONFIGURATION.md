# Session Configuration Guide

This application supports two session storage backends:
- **Redis** for local development
- **DynamoDB** for production deployment on AWS

## Configuration Overview

The session storage backend is controlled by Spring profiles:
- `dev` profile → Redis (local development)
- `prod` profile → DynamoDB (AWS production)

## Local Development (Redis)

### Prerequisites
- Redis server running on `localhost:6379`
- Start Redis: `redis-server` or use Docker: `docker run -p 6379:6379 redis`

### Configuration
The application uses Redis by default when no profile is specified or when `dev` profile is active.

**Environment Variables:**
```properties
SPRING_PROFILES_ACTIVE=dev
REDIS_HOST=localhost
REDIS_PORT=6379
```

**How it works:**
- `RedisSessionConfig.java` is activated with `@Profile({"dev", "default"})`
- Sessions are stored in Redis with 30-minute timeout
- Uses Spring Session Data Redis with Lettuce client

### Testing Locally
```bash
# Start your application (dev profile is default)
mvn spring-boot:run

# Or explicitly set dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Production Deployment (DynamoDB)

### Prerequisites
- AWS account with DynamoDB access
- IAM role with DynamoDB permissions (for EC2 instances)
- OR AWS credentials configured (for local testing)

### DynamoDB Table Setup

The application requires a DynamoDB table named `spring-sessions` with the following configuration:

**Table Name:** `spring-sessions`

**Primary Key:**
- Partition Key: `sessionId` (String)

**Attributes:**
- `sessionId` (String) - Primary key
- `creationTime` (Number) - Session creation timestamp
- `lastAccessedTime` (Number) - Last access timestamp
- `maxInactiveInterval` (Number) - Session timeout in seconds
- `expirationTime` (Number) - TTL attribute for automatic cleanup
- `attributes` (Map) - Session attributes

**Time To Live (TTL):**
- Enable TTL on `expirationTime` attribute
- DynamoDB will automatically delete expired sessions

**Create Table via AWS CLI:**
```bash
# Create the table
aws dynamodb create-table \
  --table-name spring-sessions \
  --attribute-definitions \
    AttributeName=sessionId,AttributeType=S \
  --key-schema \
    AttributeName=sessionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1

# Enable TTL for automatic session cleanup
aws dynamodb update-time-to-live \
  --table-name spring-sessions \
  --time-to-live-specification \
    Enabled=true,AttributeName=expirationTime \
  --region us-east-1
```

**Create Table via AWS Console:**
1. Go to DynamoDB console
2. Click "Create table"
3. Table name: `spring-sessions`
4. Partition key: `sessionId` (String)
5. Table settings: On-demand (or Provisioned with 5 RCU, 5 WCU)
6. Create table
7. After creation, go to "Additional settings" → "Time to Live"
8. Enable TTL with attribute name: `expirationTime`

### Configuration

**Environment Variables:**
```properties
SPRING_PROFILES_ACTIVE=prod
AWS_REGION=us-east-1
SESSION_TIMEOUT=1800
```

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
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:UpdateItem"
      ],
      "Resource": "arn:aws:dynamodb:us-east-1:*:table/spring-sessions"
    }
  ]
}
```

**How it works:**
- `DynamoDBSessionConfig.java` is activated with `@Profile("prod")`
- Sessions are stored in DynamoDB table `spring-sessions`
- Uses AWS SDK DefaultAWSCredentialsProviderChain for authentication
- Expired sessions are automatically deleted by DynamoDB TTL

### Testing Production Configuration Locally

You can test the production DynamoDB configuration locally:

1. Configure AWS credentials:
```bash
aws configure
# Enter your AWS Access Key ID
# Enter your AWS Secret Access Key
# Enter region: us-east-1
```

2. Create the DynamoDB table (see above)

3. Run with prod profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Deploying to AWS

When deploying to AWS Elastic Beanstalk or EC2:

1. **Set environment variable:**
```bash
SPRING_PROFILES_ACTIVE=prod
```

2. **Attach IAM role to EC2 instances** with DynamoDB permissions (recommended)
   - No need to configure AWS credentials
   - Uses instance metadata for authentication

3. **Ensure DynamoDB table exists** in the same region

## Cost Comparison

### Redis (ElastiCache)
- **Cost:** ~$12-15/month for cache.t3.micro
- **Free Tier:** Not available
- **Pros:** Fast, mature, widely used
- **Cons:** Costs money, requires separate service

### DynamoDB
- **Cost:** $0 (within free tier limits)
- **Free Tier:** 
  - 25 GB storage
  - 25 WCU (Write Capacity Units)
  - 25 RCU (Read Capacity Units)
- **Pros:** Free, serverless, auto-scaling, no maintenance
- **Cons:** Slightly higher latency than Redis

**Recommendation:** Use DynamoDB for production to achieve $0 deployment cost.

## Switching Between Profiles

### Local Development
```bash
# Default (uses Redis)
mvn spring-boot:run

# Explicit dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production
```bash
# Set environment variable
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run

# Or via command line
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### In IDE (IntelliJ IDEA)
1. Edit Run Configuration
2. Add VM options: `-Dspring.profiles.active=prod`
3. Or set environment variable: `SPRING_PROFILES_ACTIVE=prod`

## Troubleshooting

### Redis Connection Issues
```
Error: Unable to connect to Redis at localhost:6379
```
**Solution:** Ensure Redis is running: `redis-cli ping` should return `PONG`

### DynamoDB Access Denied
```
Error: User is not authorized to perform: dynamodb:PutItem
```
**Solution:** Check IAM permissions and ensure the role has DynamoDB access

### DynamoDB Table Not Found
```
Error: Requested resource not found: Table: spring-sessions not found
```
**Solution:** Create the DynamoDB table (see setup instructions above)

### Session Not Persisting
- Check that the correct profile is active: Look for log message indicating which config is loaded
- Verify Redis/DynamoDB is accessible
- Check session timeout configuration

## Monitoring

### Redis Sessions
```bash
# Connect to Redis
redis-cli

# List all session keys
KEYS spring:session:*

# Get session details
GET spring:session:sessions:<session-id>
```

### DynamoDB Sessions
```bash
# List all sessions
aws dynamodb scan --table-name spring-sessions

# Get specific session
aws dynamodb get-item \
  --table-name spring-sessions \
  --key '{"sessionId":{"S":"<session-id>"}}'
```

## Migration Notes

When migrating from Redis to DynamoDB (or vice versa):
1. Active sessions will be lost during the switch
2. Users will need to log in again
3. No data migration is needed
4. Both configurations can coexist (controlled by profile)

## Summary

- **Local Development:** Use `dev` profile with Redis (default)
- **Production:** Use `prod` profile with DynamoDB (free tier)
- **Cost Savings:** DynamoDB saves ~$12-15/month compared to ElastiCache Redis
- **Zero Configuration:** IAM roles handle authentication in production
- **Auto Cleanup:** DynamoDB TTL automatically removes expired sessions
