# Multi-Stage Production Dockerfile for Fitness Equipment Maintenance Portal
# Stage 1: Build & Package with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Distroless / Alpine Runtime
FROM eclipse-temurin:17-jre-alpine AS runner

LABEL org.opencontainers.image.title="Fitness Equipment Maintenance Portal" \
      org.opencontainers.image.description="DevOps Puppet-Provisioned Incident Triage & KPI Portal" \
      org.opencontainers.image.version="1.0.0-mvp" \
      org.opencontainers.image.authors="manthanrane68@gmail.com"

# Create unprivileged system group and user for container security
RUN addgroup -S portalgroup && adduser -S portaluser -G portalgroup

WORKDIR /app

# Prepare directories for data and logs with proper ownership
RUN mkdir -p /app/data /app/logs && chown -R portaluser:portalgroup /app

# Copy compiled JAR from build stage
COPY --from=builder /workspace/target/fitness-portal.jar /app/fitness-portal.jar

# Fallback copy for local pre-packaged JAR builds
# COPY target/fitness-portal.jar /app/fitness-portal.jar

USER portaluser

# Expose HTTP port
EXPOSE 8080

# Environment variables
ENV PORT=8080 \
    JAVA_OPTS="-Xms64m -Xmx256m -XX:+UseG1GC"

# Healthcheck probe to prevent traffic routing to unready containers
HEALTHCHECK --interval=20s --timeout=4s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/fitness-portal.jar"]
