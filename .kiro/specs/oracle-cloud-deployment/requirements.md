# Requirements Document

## Introduction

This specification defines the requirements for deploying a full-stack task management application (Next.js 14 frontend + Spring Boot 3.3.5 backend + PostgreSQL + Redis) to Oracle Cloud Infrastructure (OCI) using the Always Free Tier. The deployment must be production-ready, secure, and completely free forever with no credit card charges.

## Glossary

- **OCI**: Oracle Cloud Infrastructure
- **Always_Free_Tier**: Oracle Cloud's permanently free tier offering (2 AMD VMs or 4 Arm cores + 24GB RAM, never expires)
- **Deployment_System**: The complete deployment infrastructure including VM, networking, and services
- **Frontend_Service**: Next.js 14 application serving the user interface
- **Backend_Service**: Spring Boot 3.3.5 REST API application
- **Database_Service**: PostgreSQL 14 database server
- **Cache_Service**: Redis 6.x in-memory cache for session management
- **Reverse_Proxy**: Nginx server handling SSL termination and request routing
- **Process_Manager**: PM2 tool managing Next.js application lifecycle
- **Service_Manager**: Systemd managing Spring Boot application lifecycle
- **SSL_Certificate**: Let's Encrypt free SSL/TLS certificate
- **Firewall**: UFW (Uncomplicated Firewall) managing network access rules
- **Backup_System**: Automated PostgreSQL backup mechanism
- **Monitoring_System**: Logging and health monitoring infrastructure

## Requirements

### Requirement 1: Oracle Cloud Account and VM Provisioning

**User Story:** As a developer, I want to provision an Oracle Cloud Always Free Tier VM, so that I can deploy my application without any ongoing costs.

#### Acceptance Criteria

1. THE Deployment_System SHALL use Oracle Cloud Always Free Tier resources exclusively
2. THE Deployment_System SHALL provision one Ampere A1 VM with 4 OCPU cores and 24GB RAM
3. THE Deployment_System SHALL use Ubuntu 22.04 LTS as the operating system
4. THE Deployment_System SHALL configure 200GB block storage volume
5. THE Deployment_System SHALL assign one reserved public IP address
6. THE Deployment_System SHALL configure VCN (Virtual Cloud Network) with public subnet
7. THE Deployment_System SHALL configure security list to allow ingress on ports 22, 80, and 443
8. THE Deployment_System SHALL configure security list to allow all egress traffic

### Requirement 2: System Dependencies Installation

**User Story:** As a developer, I want all required software dependencies installed on the VM, so that I can run my full-stack application.

#### Acceptance Criteria

1. THE Deployment_System SHALL install OpenJDK 17 for Spring Boot execution
2. THE Deployment_System SHALL install Node.js 20 LTS for Next.js execution
3. THE Deployment_System SHALL install PostgreSQL 14 database server
4. THE Deployment_System SHALL install Redis 6.x server
5. THE Deployment_System SHALL install Nginx web server
6. THE Deployment_System SHALL install PM2 process manager globally via npm
7. THE Deployment_System SHALL install Maven 3.9+ for Java application building
8. THE Deployment_System SHALL install Certbot for SSL certificate management
9. THE Deployment_System SHALL install UFW firewall utility
10. WHEN any installation fails, THE Deployment_System SHALL log the error and halt deployment

### Requirement 3: PostgreSQL Database Configuration

**User Story:** As a developer, I want PostgreSQL properly configured with my application database, so that my backend can persist data securely.

#### Acceptance Criteria

1. THE Database_Service SHALL create a database named "task_management"
2. THE Database_Service SHALL create a dedicated database user with strong password
3. THE Database_Service SHALL grant all privileges on task_management database to the dedicated user
4. THE Database_Service SHALL configure PostgreSQL to listen on localhost only
5. THE Database_Service SHALL configure PostgreSQL to use password authentication
6. THE Database_Service SHALL set max_connections to 100
7. THE Database_Service SHALL set shared_buffers to 2GB
8. THE Database_Service SHALL enable Liquibase migrations on application startup
9. WHEN the database is created, THE Database_Service SHALL verify connectivity before proceeding

### Requirement 4: Redis Cache Configuration

**User Story:** As a developer, I want Redis configured for session management, so that my application can handle user sessions efficiently.

#### Acceptance Criteria

1. THE Cache_Service SHALL configure Redis to listen on localhost only
2. THE Cache_Service SHALL set maxmemory to 512MB
3. THE Cache_Service SHALL set maxmemory-policy to allkeys-lru
4. THE Cache_Service SHALL enable persistence with appendonly mode
5. THE Cache_Service SHALL configure appendfsync to everysec
6. THE Cache_Service SHALL disable protected mode for localhost connections
7. WHEN Redis starts, THE Cache_Service SHALL verify connectivity on port 6379

### Requirement 5: Spring Boot Backend Deployment

**User Story:** As a developer, I want my Spring Boot backend deployed as a systemd service, so that it runs reliably and restarts automatically.

#### Acceptance Criteria

1. THE Backend_Service SHALL build the Spring Boot application using Maven
2. THE Backend_Service SHALL create a dedicated system user "taskapp" for running the application
3. THE Backend_Service SHALL deploy the JAR file to /opt/taskapp/backend.jar
4. THE Backend_Service SHALL create a systemd service file at /etc/systemd/system/taskapp-backend.service
5. THE Backend_Service SHALL configure the service to start after network and database services
6. THE Backend_Service SHALL configure the service to restart automatically on failure
7. THE Backend_Service SHALL set environment variables for database connection (DB_URL, DB_USERNAME, DB_PASSWORD)
8. THE Backend_Service SHALL set environment variables for Redis connection (REDIS_HOST, REDIS_PORT)
9. THE Backend_Service SHALL set environment variables for mail configuration (MAIL_USERNAME, MAIL_PASSWORD)
10. THE Backend_Service SHALL set environment variables for OAuth2 (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)
11. THE Backend_Service SHALL set environment variables for ImageKit (IMAGEKIT_PUBLIC_KEY, IMAGEKIT_PRIVATE_KEY, IMAGEKIT_URL_ENDPOINT)
12. THE Backend_Service SHALL configure the application to listen on localhost:8080
13. THE Backend_Service SHALL configure JVM heap size to 2GB maximum
14. THE Backend_Service SHALL enable the service to start on system boot
15. WHEN the service starts, THE Backend_Service SHALL log startup events to /var/log/taskapp/backend.log

### Requirement 6: Next.js Frontend Deployment

**User Story:** As a developer, I want my Next.js frontend deployed with PM2, so that it serves users reliably with automatic restarts.

#### Acceptance Criteria

1. THE Frontend_Service SHALL build the Next.js application in production mode
2. THE Frontend_Service SHALL deploy the built application to /opt/taskapp/frontend
3. THE Frontend_Service SHALL configure PM2 to run "npm start" in the frontend directory
4. THE Frontend_Service SHALL configure PM2 with application name "taskapp-frontend"
5. THE Frontend_Service SHALL configure PM2 to use 2 instances in cluster mode
6. THE Frontend_Service SHALL configure PM2 to restart on file changes
7. THE Frontend_Service SHALL configure PM2 to restart automatically on failure
8. THE Frontend_Service SHALL configure the application to listen on localhost:3000
9. THE Frontend_Service SHALL configure PM2 to start on system boot using pm2 startup
10. THE Frontend_Service SHALL save the PM2 process list using pm2 save
11. WHEN the application starts, THE Frontend_Service SHALL log events to PM2 logs

### Requirement 7: Nginx Reverse Proxy Configuration

**User Story:** As a developer, I want Nginx configured as a reverse proxy, so that my frontend and backend are accessible through a single domain with proper routing.

#### Acceptance Criteria

1. THE Reverse_Proxy SHALL configure a server block for the application domain
2. THE Reverse_Proxy SHALL proxy requests to "/" to Frontend_Service on localhost:3000
3. THE Reverse_Proxy SHALL proxy requests to "/api" to Backend_Service on localhost:8080
4. THE Reverse_Proxy SHALL strip "/api" prefix when forwarding to backend
5. THE Reverse_Proxy SHALL set proxy headers (Host, X-Real-IP, X-Forwarded-For, X-Forwarded-Proto)
6. THE Reverse_Proxy SHALL configure client_max_body_size to 25MB for file uploads
7. THE Reverse_Proxy SHALL enable gzip compression for text-based responses
8. THE Reverse_Proxy SHALL configure proxy timeouts to 60 seconds
9. THE Reverse_Proxy SHALL configure WebSocket support with Connection and Upgrade headers
10. THE Reverse_Proxy SHALL disable server tokens for security
11. WHEN configuration is complete, THE Reverse_Proxy SHALL validate syntax using nginx -t

### Requirement 8: SSL Certificate Configuration

**User Story:** As a developer, I want free SSL certificates from Let's Encrypt, so that my application is accessible over HTTPS securely.

#### Acceptance Criteria

1. THE Deployment_System SHALL install Certbot with Nginx plugin
2. THE SSL_Certificate SHALL be obtained using Certbot for the configured domain
3. THE SSL_Certificate SHALL use HTTP-01 challenge for domain validation
4. THE Reverse_Proxy SHALL redirect all HTTP traffic to HTTPS
5. THE Reverse_Proxy SHALL configure SSL protocols to TLSv1.2 and TLSv1.3 only
6. THE Reverse_Proxy SHALL configure strong SSL ciphers
7. THE Reverse_Proxy SHALL enable HSTS (HTTP Strict Transport Security) with max-age of 31536000
8. THE Reverse_Proxy SHALL configure SSL session cache and timeout
9. THE SSL_Certificate SHALL auto-renew using Certbot's systemd timer
10. WHEN certificate is obtained, THE Deployment_System SHALL verify HTTPS access

### Requirement 9: Firewall Configuration

**User Story:** As a developer, I want a properly configured firewall, so that my application is protected from unauthorized access.

#### Acceptance Criteria

1. THE Firewall SHALL allow incoming SSH connections on port 22
2. THE Firewall SHALL allow incoming HTTP connections on port 80
3. THE Firewall SHALL allow incoming HTTPS connections on port 443
4. THE Firewall SHALL deny all other incoming connections by default
5. THE Firewall SHALL allow all outgoing connections
6. THE Firewall SHALL enable UFW on system boot
7. THE Firewall SHALL log denied connection attempts
8. WHEN firewall rules are applied, THE Firewall SHALL verify active status

### Requirement 10: Database Backup System

**User Story:** As a developer, I want automated PostgreSQL backups, so that I can recover data in case of failure.

#### Acceptance Criteria

1. THE Backup_System SHALL create a backup script at /opt/taskapp/scripts/backup-db.sh
2. THE Backup_System SHALL use pg_dump to create SQL backup files
3. THE Backup_System SHALL compress backups using gzip
4. THE Backup_System SHALL store backups in /opt/taskapp/backups directory
5. THE Backup_System SHALL include timestamp in backup filename
6. THE Backup_System SHALL retain backups for 7 days
7. THE Backup_System SHALL delete backups older than 7 days automatically
8. THE Backup_System SHALL schedule daily backups at 2:00 AM using cron
9. THE Backup_System SHALL log backup operations to /var/log/taskapp/backup.log
10. WHEN backup completes, THE Backup_System SHALL verify backup file integrity

### Requirement 11: Monitoring and Logging

**User Story:** As a developer, I want centralized logging and monitoring, so that I can troubleshoot issues and monitor application health.

#### Acceptance Criteria

1. THE Monitoring_System SHALL create log directory at /var/log/taskapp
2. THE Monitoring_System SHALL configure Backend_Service to log to /var/log/taskapp/backend.log
3. THE Monitoring_System SHALL configure log rotation for backend logs (daily, keep 14 days)
4. THE Monitoring_System SHALL configure log rotation for Nginx logs (daily, keep 14 days)
5. THE Monitoring_System SHALL create a health check script at /opt/taskapp/scripts/health-check.sh
6. THE Monitoring_System SHALL verify Backend_Service responds on localhost:8080/actuator/health
7. THE Monitoring_System SHALL verify Frontend_Service responds on localhost:3000
8. THE Monitoring_System SHALL verify Database_Service accepts connections
9. THE Monitoring_System SHALL verify Cache_Service responds to PING command
10. THE Monitoring_System SHALL schedule health checks every 5 minutes using cron
11. WHEN health check fails, THE Monitoring_System SHALL log failure details

### Requirement 12: Environment Configuration Management

**User Story:** As a developer, I want secure environment variable management, so that sensitive credentials are protected and easily configurable.

#### Acceptance Criteria

1. THE Deployment_System SHALL create environment file at /opt/taskapp/.env
2. THE Deployment_System SHALL set file permissions to 600 (owner read/write only)
3. THE Deployment_System SHALL set file owner to taskapp user
4. THE Deployment_System SHALL store database credentials in environment file
5. THE Deployment_System SHALL store Redis configuration in environment file
6. THE Deployment_System SHALL store mail credentials in environment file
7. THE Deployment_System SHALL store OAuth2 credentials in environment file
8. THE Deployment_System SHALL store ImageKit credentials in environment file
9. THE Deployment_System SHALL store Google Calendar credentials path in environment file
10. THE Backend_Service SHALL load environment variables from /opt/taskapp/.env on startup
11. WHEN environment file is created, THE Deployment_System SHALL validate all required variables are present

### Requirement 13: Deployment Automation Script

**User Story:** As a developer, I want an automated deployment script, so that I can deploy or update the application with a single command.

#### Acceptance Criteria

1. THE Deployment_System SHALL create deployment script at /opt/taskapp/scripts/deploy.sh
2. THE Deployment_System SHALL make the script executable
3. WHEN the script runs, THE Deployment_System SHALL pull latest code from Git repository
4. WHEN the script runs, THE Deployment_System SHALL build the Spring Boot backend
5. WHEN the script runs, THE Deployment_System SHALL build the Next.js frontend
6. WHEN the script runs, THE Deployment_System SHALL stop Backend_Service gracefully
7. WHEN the script runs, THE Deployment_System SHALL stop Frontend_Service gracefully
8. WHEN the script runs, THE Deployment_System SHALL deploy new backend JAR file
9. WHEN the script runs, THE Deployment_System SHALL deploy new frontend build
10. WHEN the script runs, THE Deployment_System SHALL start Backend_Service
11. WHEN the script runs, THE Deployment_System SHALL start Frontend_Service
12. WHEN the script runs, THE Deployment_System SHALL verify both services are healthy
13. IF deployment fails, THEN THE Deployment_System SHALL rollback to previous version
14. WHEN deployment completes, THE Deployment_System SHALL log deployment timestamp and status

### Requirement 14: Security Hardening

**User Story:** As a developer, I want the deployment hardened against common security threats, so that my application and data are protected.

#### Acceptance Criteria

1. THE Deployment_System SHALL disable root SSH login
2. THE Deployment_System SHALL configure SSH to use key-based authentication only
3. THE Deployment_System SHALL install and configure fail2ban for SSH brute force protection
4. THE Deployment_System SHALL configure automatic security updates for Ubuntu
5. THE Deployment_System SHALL set restrictive file permissions on application directories (750)
6. THE Deployment_System SHALL set restrictive file permissions on configuration files (640)
7. THE Deployment_System SHALL configure PostgreSQL to reject remote connections
8. THE Deployment_System SHALL configure Redis to reject remote connections
9. THE Deployment_System SHALL disable unnecessary system services
10. THE Deployment_System SHALL configure kernel parameters for network security (syn cookies, IP forwarding disabled)
11. WHEN security hardening completes, THE Deployment_System SHALL verify all security measures are active

### Requirement 15: Documentation and Maintenance Procedures

**User Story:** As a developer, I want comprehensive documentation, so that I can maintain and troubleshoot the deployment effectively.

#### Acceptance Criteria

1. THE Deployment_System SHALL create README.md at /opt/taskapp/README.md
2. THE Deployment_System SHALL document Oracle Cloud account setup procedure
3. THE Deployment_System SHALL document VM provisioning steps with screenshots
4. THE Deployment_System SHALL document all installed software versions
5. THE Deployment_System SHALL document environment variable configuration
6. THE Deployment_System SHALL document service management commands (start, stop, restart, status)
7. THE Deployment_System SHALL document log file locations
8. THE Deployment_System SHALL document backup and restore procedures
9. THE Deployment_System SHALL document SSL certificate renewal procedure
10. THE Deployment_System SHALL document common troubleshooting scenarios and solutions
11. THE Deployment_System SHALL document how to update application code
12. THE Deployment_System SHALL document how to scale resources if needed
13. THE Deployment_System SHALL document monitoring and health check procedures
14. THE Deployment_System SHALL document security best practices and maintenance tasks
