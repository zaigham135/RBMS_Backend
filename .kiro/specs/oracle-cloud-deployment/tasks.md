# Implementation Plan: Oracle Cloud Always Free Deployment

## Overview

This implementation plan provides step-by-step tasks for deploying a full-stack task management application (Next.js 14 frontend + Spring Boot 3.3.5 backend + PostgreSQL + Redis) to Oracle Cloud Infrastructure Always Free Tier. The deployment uses a single Ampere A1 VM (4 OCPU, 24GB RAM) running Ubuntu 22.04 LTS with production-ready configuration including SSL, monitoring, backups, and security hardening.

## Tasks

### Phase 1: Oracle Cloud Account and VM Setup

- [ ] 1. Create Oracle Cloud account and provision VM
  - [ ] 1.1 Create Oracle Cloud Always Free account
    - Navigate to https://www.oracle.com/cloud/free/
    - Complete registration with email, password, country/region
    - Complete phone verification and credit card verification (for identity only)
    - Verify "Account Type: Free Tier" in Account > Tenancy Details
    - _Requirements: 1.1, 1.2_
  
  - [ ] 1.2 Create Virtual Cloud Network (VCN)
    - Navigate to Networking > Virtual Cloud Networks in OCI Console
    - Use "Start VCN Wizard" > "Create VCN with Internet Connectivity"
    - Configure VCN name as "taskapp-vcn", CIDR block 10.0.0.0/16
    - Configure public subnet CIDR as 10.0.0.0/24
    - _Requirements: 1.6_
  
  - [ ] 1.3 Configure Security List rules
    - Navigate to VCN > Security Lists > Default Security List
    - Add ingress rule for SSH: Source 0.0.0.0/0, TCP, Port 22
    - Add ingress rule for HTTP: Source 0.0.0.0/0, TCP, Port 80
    - Add ingress rule for HTTPS: Source 0.0.0.0/0, TCP, Port 443
    - Verify egress rules allow all traffic (default)
    - _Requirements: 1.7, 1.8_
  
  - [ ] 1.4 Create Ampere A1 Compute Instance
    - Navigate to Compute > Instances > Create Instance
    - Set name to "taskapp-vm", select Availability Domain
    - Select Image: Canonical Ubuntu 22.04 (ARM64)
    - Select Shape: VM.Standard.A1.Flex with 4 OCPU and 24GB RAM
    - Configure networking: VCN "taskapp-vcn", public subnet, assign reserved public IP
    - Upload SSH public key or generate new key pair
    - Set boot volume size to 200GB
    - Wait for instance state: RUNNING
    - Note the public IP address
    - _Requirements: 1.2, 1.3, 1.4, 1.5_
  
  - [ ] 1.5 Configure DNS for domain
    - In domain registrar, add A record pointing to VM public IP
    - Add A record for www subdomain (optional)
    - Set TTL to 300 seconds
    - Verify DNS propagation using `dig yourdomain.com +short`
    - _Requirements: 8.2_

- [ ] 2. Checkpoint - Verify VM access
  - Ensure SSH connection works to VM using public IP
  - Ensure DNS resolves to correct IP address
  - Ask user if questions arise

### Phase 2: System Dependencies Installation

- [ ] 3. Perform initial server setup and install system dependencies
  - [ ] 3.1 Connect to VM and update system
    - Connect via SSH: `ssh -i ~/.ssh/id_rsa ubuntu@<VM-PUBLIC-IP>`
    - Run system update: `sudo apt update && sudo apt upgrade -y`
    - Install basic utilities: `sudo apt install -y curl wget git vim htop unzip`
    - _Requirements: 2.1_
  
  - [ ] 3.2 Configure UFW firewall
    - Install UFW: `sudo apt install -y ufw`
    - Set default policies: deny incoming, allow outgoing
    - Allow SSH (port 22), HTTP (port 80), HTTPS (port 443)
    - Enable UFW and verify status
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_
  
  - [ ] 3.3 Install OpenJDK 17
    - Install OpenJDK 17: `sudo apt install -y openjdk-17-jdk`
    - Verify installation: `java -version`
    - Set JAVA_HOME environment variable in /etc/environment
    - _Requirements: 2.1_
  
  - [ ] 3.4 Install Node.js 20 LTS
    - Add NodeSource repository for Node.js 20
    - Install Node.js: `sudo apt install -y nodejs`
    - Verify installation: `node --version` and `npm --version`
    - _Requirements: 2.2_
  
  - [ ] 3.5 Install PostgreSQL 14
    - Install PostgreSQL: `sudo apt install -y postgresql postgresql-contrib`
    - Verify service status and version
    - _Requirements: 2.3_
  
  - [ ] 3.6 Install Redis 6.x
    - Install Redis: `sudo apt install -y redis-server`
    - Verify service status and version
    - _Requirements: 2.4_
  
  - [ ] 3.7 Install Nginx
    - Install Nginx: `sudo apt install -y nginx`
    - Verify service status and version
    - _Requirements: 2.5_
  
  - [ ] 3.8 Install PM2 and Maven
    - Install PM2 globally: `sudo npm install -g pm2`
    - Install Maven: `sudo apt install -y maven`
    - Verify installations: `pm2 --version` and `mvn --version`
    - _Requirements: 2.6, 2.7_
  
  - [ ] 3.9 Install Certbot for SSL certificates
    - Install Certbot with Nginx plugin: `sudo apt install -y certbot python3-certbot-nginx`
    - Verify installation: `certbot --version`
    - _Requirements: 2.8_

- [ ] 4. Checkpoint - Verify all dependencies installed
  - Ensure all services are installed and running
  - Verify versions match requirements
  - Ask user if questions arise

### Phase 3: Database and Redis Configuration

- [ ] 5. Configure PostgreSQL database
  - [ ] 5.1 Create database and user
    - Switch to postgres user and create database "task_management"
    - Create dedicated user "taskapp_user" with strong password
    - Grant all privileges on database to taskapp_user
    - Grant schema permissions
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [ ] 5.2 Configure PostgreSQL settings
    - Edit /etc/postgresql/14/main/postgresql.conf
    - Set listen_addresses to 'localhost'
    - Configure memory settings: shared_buffers=2GB, effective_cache_size=6GB
    - Set max_connections to 100
    - Configure WAL and query tuning parameters
    - _Requirements: 3.4, 3.6, 3.7_
  
  - [ ] 5.3 Configure PostgreSQL authentication
    - Edit /etc/postgresql/14/main/pg_hba.conf
    - Configure localhost-only connections with md5 authentication
    - Restart PostgreSQL service
    - Verify connection: `psql -U taskapp_user -d task_management -h localhost`
    - _Requirements: 3.5, 3.9_

- [ ] 6. Configure Redis cache
  - [ ] 6.1 Configure Redis settings
    - Edit /etc/redis/redis.conf
    - Set bind to 127.0.0.1 (localhost only)
    - Configure maxmemory to 512MB with allkeys-lru policy
    - Enable persistence: appendonly yes, appendfsync everysec
    - Configure save points for RDB snapshots
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [ ] 6.2 Restart Redis and verify connectivity
    - Restart Redis service
    - Verify connection: `redis-cli ping` (should return PONG)
    - _Requirements: 4.6, 4.7_

- [ ] 7. Checkpoint - Verify database and cache configuration
  - Ensure PostgreSQL accepts connections from taskapp_user
  - Ensure Redis responds to ping command
  - Verify both services listen on localhost only
  - Ask user if questions arise

### Phase 4: Backend Deployment (systemd)

- [ ] 8. Create application user and directory structure
  - [ ] 8.1 Create taskapp system user
    - Create user: `sudo useradd -r -s /bin/bash -d /opt/taskapp -m taskapp`
    - Create directory structure: /opt/taskapp/{backups,scripts,config}
    - Create log directory: /var/log/taskapp
    - Set ownership to taskapp:taskapp
    - Set permissions: 750 for directories
    - _Requirements: 5.2, 12.1_

- [ ] 9. Build and deploy Spring Boot backend
  - [ ] 9.1 Build backend JAR file
    - Clone backend repository to /tmp
    - Navigate to backend directory
    - Build with Maven: `mvn clean package -DskipTests`
    - Copy JAR to /opt/taskapp/backend.jar
    - Set ownership to taskapp:taskapp
    - _Requirements: 5.1_
  
  - [ ] 9.2 Create environment configuration file
    - Create /opt/taskapp/.env file
    - Add all required environment variables: DB_URL, DB_USERNAME, DB_PASSWORD
    - Add Redis configuration: REDIS_HOST, REDIS_PORT
    - Add mail configuration: MAIL_USERNAME, MAIL_PASSWORD
    - Add OAuth2 credentials: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
    - Add ImageKit credentials: IMAGEKIT_PUBLIC_KEY, IMAGEKIT_PRIVATE_KEY, IMAGEKIT_URL_ENDPOINT
    - Add Google Calendar credentials path
    - Add application settings: SERVER_PORT=8080, SPRING_PROFILES_ACTIVE=prod
    - Set file permissions to 600 (owner read/write only)
    - Set ownership to taskapp:taskapp
    - _Requirements: 5.7, 5.8, 5.9, 5.10, 5.11, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9, 12.10_
  
  - [ ] 9.3 Copy Google Calendar credentials
    - Upload google-credentials.json to server
    - Move to /opt/taskapp/config/google-credentials.json
    - Set ownership to taskapp:taskapp
    - Set permissions to 600
    - _Requirements: 5.11_
  
  - [ ] 9.4 Create systemd service for backend
    - Create /etc/systemd/system/taskapp-backend.service
    - Configure service: Type=simple, User=taskapp, WorkingDirectory=/opt/taskapp
    - Set EnvironmentFile=/opt/taskapp/.env
    - Configure ExecStart with Java command: -Xmx2g -Xms512m -jar backend.jar
    - Configure automatic restart: Restart=always, RestartSec=10
    - Set service dependencies: After=network.target postgresql.service redis.service
    - Configure logging to /var/log/taskapp/backend.log
    - Add security settings: NoNewPrivileges=true, PrivateTmp=true
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.13, 5.14, 5.15_
  
  - [ ] 9.5 Enable and start backend service
    - Reload systemd: `sudo systemctl daemon-reload`
    - Enable service: `sudo systemctl enable taskapp-backend`
    - Start service: `sudo systemctl start taskapp-backend`
    - Verify service status: `sudo systemctl status taskapp-backend`
    - Check logs: `sudo tail -f /var/log/taskapp/backend.log`
    - Verify backend responds: `curl http://localhost:8080/actuator/health`
    - _Requirements: 5.14, 5.15_

- [ ] 10. Checkpoint - Verify backend deployment
  - Ensure backend service is running and enabled
  - Ensure backend responds on localhost:8080
  - Verify database connection works
  - Verify Redis connection works
  - Ask user if questions arise

### Phase 5: Frontend Deployment (PM2)

- [ ] 11. Build and deploy Next.js frontend
  - [ ] 11.1 Build frontend application
    - Clone frontend repository to /tmp
    - Navigate to frontend directory
    - Install dependencies: `npm install`
    - Create production build: `npm run build`
    - Copy build files to /opt/taskapp/frontend/
    - Copy .next, node_modules, package.json, package-lock.json
    - Set ownership to taskapp:taskapp
    - _Requirements: 6.1, 6.2_
  
  - [ ] 11.2 Create PM2 ecosystem configuration
    - Create /opt/taskapp/frontend/ecosystem.config.js
    - Configure app name: "taskapp-frontend"
    - Set working directory: /opt/taskapp/frontend
    - Configure script: npm start
    - Set instances to 2 in cluster mode
    - Configure environment variables: NODE_ENV=production, PORT=3000, NEXT_PUBLIC_API_URL
    - Configure logging to /var/log/taskapp/frontend-*.log
    - Set max_memory_restart to 1G
    - Configure autorestart and max_restarts
    - Set ownership to taskapp:taskapp
    - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_
  
  - [ ] 11.3 Start frontend with PM2
    - Switch to taskapp user
    - Start application: `pm2 start ecosystem.config.js`
    - Save PM2 process list: `pm2 save`
    - Configure PM2 startup: `pm2 startup systemd -u taskapp --hp /opt/taskapp`
    - Verify PM2 status: `pm2 status`
    - Verify frontend responds: `curl http://localhost:3000`
    - _Requirements: 6.9, 6.10, 6.11_

- [ ] 12. Checkpoint - Verify frontend deployment
  - Ensure PM2 is running frontend with 2 instances
  - Ensure frontend responds on localhost:3000
  - Verify PM2 configured to start on boot
  - Ask user if questions arise

### Phase 6: Nginx and SSL Configuration

- [ ] 13. Configure Nginx reverse proxy
  - [ ] 13.1 Create Nginx site configuration
    - Create /etc/nginx/sites-available/taskapp
    - Configure HTTP server block listening on port 80
    - Configure location / to proxy to localhost:3000 (frontend)
    - Configure location /api to proxy to localhost:8080 (backend)
    - Strip /api prefix when forwarding to backend
    - Set proxy headers: Host, X-Real-IP, X-Forwarded-For, X-Forwarded-Proto
    - Configure WebSocket support with Upgrade and Connection headers
    - Set client_max_body_size to 25MB
    - Enable gzip compression for text-based responses
    - Set proxy timeouts to 60 seconds
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9_
  
  - [ ] 13.2 Enable Nginx site and test configuration
    - Create symlink: /etc/nginx/sites-enabled/taskapp
    - Remove default site: /etc/nginx/sites-enabled/default
    - Test configuration: `sudo nginx -t`
    - Reload Nginx: `sudo systemctl reload nginx`
    - _Requirements: 7.11_

- [ ] 14. Obtain and configure SSL certificate
  - [ ] 14.1 Obtain Let's Encrypt SSL certificate
    - Run Certbot: `sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com`
    - Enter email address for renewal notifications
    - Agree to terms of service
    - Choose to redirect HTTP to HTTPS
    - Verify certificate obtained successfully
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [ ] 14.2 Enhance Nginx SSL and security configuration
    - Edit /etc/nginx/sites-available/taskapp
    - Verify SSL protocols set to TLSv1.2 and TLSv1.3 only
    - Verify strong SSL ciphers configured
    - Add HSTS header with max-age=31536000
    - Add X-Frame-Options: SAMEORIGIN header
    - Add X-Content-Type-Options: nosniff header
    - Disable server_tokens to hide Nginx version
    - Configure SSL session cache and timeout
    - Test configuration: `sudo nginx -t`
    - Reload Nginx: `sudo systemctl reload nginx`
    - _Requirements: 7.10, 8.5, 8.6, 8.7, 8.8_
  
  - [ ] 14.3 Verify SSL certificate auto-renewal
    - Check Certbot timer status: `sudo systemctl status certbot.timer`
    - Test renewal: `sudo certbot renew --dry-run`
    - Verify timer is enabled for automatic renewal
    - _Requirements: 8.9, 8.10_

- [ ] 15. Checkpoint - Verify Nginx and SSL configuration
  - Ensure HTTPS access works: `curl -I https://yourdomain.com`
  - Ensure HTTP redirects to HTTPS
  - Verify frontend accessible via domain
  - Verify backend API accessible via domain/api
  - Test SSL certificate validity
  - Ask user if questions arise

### Phase 7: Backup and Monitoring Setup

- [ ] 16. Create database backup system
  - [ ] 16.1 Create backup script
    - Create /opt/taskapp/scripts/backup-db.sh
    - Implement pg_dump with gzip compression
    - Store backups in /opt/taskapp/backups with timestamp
    - Implement 7-day retention (delete older backups)
    - Log operations to /var/log/taskapp/backup.log
    - Verify backup file integrity after creation
    - Make script executable and set ownership to taskapp
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.9, 10.10_
  
  - [ ] 16.2 Schedule daily backups with cron
    - Edit crontab for taskapp user
    - Add cron job: daily backup at 2:00 AM
    - Verify cron job added: `sudo crontab -u taskapp -l`
    - _Requirements: 10.8_
  
  - [ ] 16.3 Test backup script
    - Run backup manually: `sudo -u taskapp /opt/taskapp/scripts/backup-db.sh`
    - Verify backup file created in /opt/taskapp/backups
    - Check backup log: `tail /var/log/taskapp/backup.log`
    - Verify backup file is not empty and is gzipped
    - _Requirements: 10.10_

- [ ] 17. Create health monitoring system
  - [ ] 17.1 Create health check script
    - Create /opt/taskapp/scripts/health-check.sh
    - Check backend health: curl localhost:8080/actuator/health
    - Check frontend availability: curl localhost:3000
    - Check PostgreSQL connectivity: psql connection test
    - Check Redis connectivity: redis-cli ping
    - Log results to /var/log/taskapp/health-check.log
    - Make script executable and set ownership to taskapp
    - _Requirements: 11.5, 11.6, 11.7, 11.8, 11.9, 11.11_
  
  - [ ] 17.2 Schedule health checks with cron
    - Edit crontab for taskapp user
    - Add cron job: health check every 5 minutes
    - Verify cron job added
    - _Requirements: 11.10_
  
  - [ ] 17.3 Test health check script
    - Run health check manually: `/opt/taskapp/scripts/health-check.sh`
    - Verify all services report OK
    - Check health check log: `tail /var/log/taskapp/health-check.log`
    - _Requirements: 11.11_

- [ ] 18. Configure centralized logging
  - [ ] 18.1 Set up log directory and permissions
    - Verify /var/log/taskapp directory exists
    - Verify ownership is taskapp:taskapp
    - Verify backend logs to /var/log/taskapp/backend.log
    - Verify PM2 logs to /var/log/taskapp/frontend-*.log
    - _Requirements: 11.1, 11.2_
  
  - [ ] 18.2 Configure log rotation
    - Create /etc/logrotate.d/taskapp configuration
    - Configure daily rotation with 14-day retention
    - Enable compression with delaycompress
    - Configure postrotate script to reload services
    - Test logrotate: `sudo logrotate -d /etc/logrotate.d/taskapp`
    - _Requirements: 11.3, 11.4_

- [ ] 19. Checkpoint - Verify backup and monitoring
  - Ensure backup script runs successfully
  - Ensure health check script runs successfully
  - Verify cron jobs are scheduled
  - Verify log rotation configured
  - Ask user if questions arise

### Phase 8: Security Hardening

- [ ] 20. Harden SSH and system security
  - [ ] 20.1 Configure SSH security
    - Edit /etc/ssh/sshd_config
    - Disable root login: PermitRootLogin no
    - Disable password authentication: PasswordAuthentication no
    - Enable public key authentication only: PubkeyAuthentication yes
    - Disable challenge-response authentication
    - Restart SSH service
    - _Requirements: 14.1, 14.2_
  
  - [ ] 20.2 Install and configure fail2ban
    - Install fail2ban: `sudo apt install -y fail2ban`
    - Create /etc/fail2ban/jail.local configuration
    - Configure SSH jail: bantime=3600, findtime=600, maxretry=5
    - Enable and start fail2ban service
    - Verify status: `sudo fail2ban-client status sshd`
    - _Requirements: 14.3_
  
  - [ ] 20.3 Configure automatic security updates
    - Install unattended-upgrades: `sudo apt install -y unattended-upgrades`
    - Enable automatic updates: `sudo dpkg-reconfigure -plow unattended-upgrades`
    - Verify configuration in /etc/apt/apt.conf.d/20auto-upgrades
    - _Requirements: 14.4_

- [ ] 21. Apply file and service security hardening
  - [ ] 21.1 Set restrictive file permissions
    - Set /opt/taskapp directory permissions to 750
    - Set /opt/taskapp/.env permissions to 600
    - Set /opt/taskapp/config/* permissions to 600
    - Set script files to 750 (executable)
    - Verify all files owned by taskapp:taskapp
    - _Requirements: 14.5, 14.6_
  
  - [ ] 21.2 Verify database and cache security
    - Verify PostgreSQL listens on localhost only
    - Verify Redis listens on localhost only
    - Test external connections are rejected
    - _Requirements: 14.7, 14.8_
  
  - [ ] 21.3 Disable unnecessary services and harden kernel
    - Identify and disable unnecessary system services
    - Configure kernel parameters: enable syn cookies, disable IP forwarding
    - Edit /etc/sysctl.conf and apply changes
    - _Requirements: 14.9, 14.10_
  
  - [ ] 21.4 Verify all security measures
    - Run security checklist verification
    - Verify UFW firewall active and configured
    - Verify fail2ban active and monitoring SSH
    - Verify SSH hardening applied
    - Verify file permissions correct
    - Verify services listen on localhost only
    - _Requirements: 14.11_

- [ ] 22. Checkpoint - Verify security hardening
  - Ensure SSH key-only authentication works
  - Ensure fail2ban is active and configured
  - Ensure automatic updates enabled
  - Verify all file permissions correct
  - Verify database and Redis not accessible externally
  - Ask user if questions arise

### Phase 9: Testing and Verification

- [ ] 23. Perform comprehensive deployment testing
  - [ ] 23.1 Test service availability
    - Verify all systemd services running: backend, postgresql, redis, nginx
    - Verify PM2 frontend running with 2 instances
    - Test backend health endpoint: `curl https://yourdomain.com/api/actuator/health`
    - Test frontend accessibility: `curl -I https://yourdomain.com`
    - _Requirements: All_
  
  - [ ] 23.2 Test application functionality
    - Test user registration via API
    - Test user login via API
    - Test file upload functionality
    - Verify frontend loads in browser
    - Test navigation and basic features
    - _Requirements: All_
  
  - [ ] 23.3 Test security configuration
    - Run nmap scan to verify only ports 22, 80, 443 open
    - Verify PostgreSQL port 5432 not accessible externally
    - Verify Redis port 6379 not accessible externally
    - Test SSL certificate validity and strength
    - Verify HTTPS redirect from HTTP
    - Test SSH key-only authentication
    - _Requirements: 9.1, 9.2, 9.3, 14.7, 14.8_
  
  - [ ] 23.4 Test backup and recovery
    - Run backup script manually
    - Verify backup file created and not empty
    - Test backup integrity by examining contents
    - Document backup restoration procedure
    - _Requirements: 10.1-10.10_
  
  - [ ] 23.5 Test monitoring and logging
    - Run health check script manually
    - Verify all services report healthy
    - Check all log files are being written
    - Verify log rotation configuration
    - Test cron jobs are scheduled correctly
    - _Requirements: 11.1-11.11_
  
  - [ ] 23.6 Perform basic load testing
    - Check memory usage: `free -h` (should be under 20GB)
    - Check CPU usage: `top` (load average should be under 4.0)
    - Check disk usage: `df -h` (should be under 80%)
    - Run basic load test with Apache Bench (ab) on frontend and backend
    - _Requirements: All_

- [ ] 24. Create deployment documentation
  - [ ] 24.1 Create comprehensive README
    - Create /opt/taskapp/README.md
    - Document Oracle Cloud account setup procedure
    - Document VM provisioning steps
    - Document all installed software versions
    - Document environment variable configuration
    - Document service management commands (start, stop, restart, status)
    - Document log file locations
    - Document backup and restore procedures
    - Document SSL certificate renewal procedure
    - Document common troubleshooting scenarios
    - Document update procedures
    - Document scaling considerations
    - Document monitoring procedures
    - Document security best practices and maintenance tasks
    - _Requirements: 15.1-15.14_

- [ ] 25. Create deployment automation script
  - [ ] 25.1 Create automated deployment script
    - Create /opt/taskapp/scripts/deploy.sh
    - Implement backup of current version before deployment
    - Implement git clone of repository
    - Implement backend build with Maven
    - Implement frontend build with npm
    - Implement service stop, file replacement, service start
    - Implement health check after deployment
    - Implement rollback on failure
    - Implement cleanup of temporary files
    - Log all operations to /var/log/taskapp/deploy.log
    - Make script executable
    - _Requirements: 13.1-13.14_
  
  - [ ] 25.2 Test deployment script
    - Run deployment script in test mode
    - Verify backup created before deployment
    - Verify services restart correctly
    - Verify rollback works on simulated failure
    - _Requirements: 13.1-13.14_

- [ ] 26. Final checkpoint - Complete deployment verification
  - Ensure all services running and healthy
  - Ensure application accessible via HTTPS
  - Ensure all security measures in place
  - Ensure backups scheduled and working
  - Ensure monitoring active
  - Ensure documentation complete
  - Ask user to perform final acceptance testing

## Notes

- This deployment uses only Oracle Cloud Always Free Tier resources (no charges)
- All services run on a single Ampere A1 VM with 4 OCPU and 24GB RAM
- The deployment is production-ready with SSL, monitoring, backups, and security hardening
- Checkpoints are included at reasonable breaks for validation
- All tasks reference specific requirements for traceability
- The deployment uses Bash shell scripts for automation
- No property-based testing is included as this is infrastructure deployment (IaC)
- Testing focuses on integration tests, security tests, and functional verification
