# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy generic-framework first (shared dependency)
COPY generic-framework /app/generic-framework

# Install generic-framework to local Maven repo
RUN cd /app/generic-framework && \
    chmod +x ../mvnw 2>/dev/null || true

# Copy Maven wrapper and pom.xml for dependency caching
COPY api-gateway/pom.xml /app/api-gateway/
COPY api-gateway/.mvn /app/api-gateway/.mvn/ 2>/dev/null || true
COPY api-gateway/mvnw /app/api-gateway/ 2>/dev/null || true

WORKDIR /app/api-gateway

# Install Maven if not using wrapper
RUN apk add --no-cache maven

# Install generic-framework dependency
RUN cd /app/generic-framework && mvn clean install -DskipTests -q

# Copy source code
COPY api-gateway/src /app/api-gateway/src

# Build the application
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built artifact
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 7070

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:7070/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

