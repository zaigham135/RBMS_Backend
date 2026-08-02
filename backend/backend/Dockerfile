# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the jar (skip tests for faster builds)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from the builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Render uses PORT env variable — default to 8080
EXPOSE 8080

# JVM tuning for container environments (memory limits awareness)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
