# ==============================================================================
# PlaySwap Modular Monolith - Multi-stage Dockerfile
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Resolve dependencies in offline mode
RUN ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build production JAR (tests run during CI/test phase, skipping during image build for speed)
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Production Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="PlaySwap Engineering Team"
LABEL description="PlaySwap Backend - Unified Modular Monolith"

WORKDIR /app

# Install curl for health checks and create a dedicated non-root user/group
RUN apk add --no-cache curl \
    && addgroup -S playswap && adduser -S playswap -G playswap

# Copy the built JAR from the builder stage
COPY --from=builder --chown=playswap:playswap /build/target/playswap-backend-1.0.0.jar /app/app.jar

# Switch to non-root user for security hardening
USER playswap:playswap

# Expose the unified application port (matches API Gateway port 9000)
EXPOSE 9000

# JVM memory and GC configuration tuned for 1-2GB RAM VMs:
# - MaxRAMPercentage=75.0: Dynamically scales heap to 75% of container memory limit
# - UseG1GC: Low-latency garbage collection with small heap footprint
# - ExitOnOutOfMemoryError: Fail fast so container orchestrator/Docker can restart cleanly
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Healthcheck hitting Actuator health endpoint
HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:9000/actuator/health || exit 1

# Launch application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
