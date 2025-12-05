# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy generic-framework first (shared dependency)
COPY generic-framework /app/generic-framework

# Copy Maven wrapper and pom.xml for dependency caching
COPY member/pom.xml /app/member/
COPY member/.mvn /app/member/.mvn/ 2>/dev/null || true
COPY member/mvnw /app/member/ 2>/dev/null || true

WORKDIR /app/member

# Install Maven if not using wrapper
RUN apk add --no-cache maven

# Install generic-framework dependency
RUN cd /app/generic-framework && mvn clean install -DskipTests -q

# Copy source code
COPY member/src /app/member/src

# Build the application
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directory for JavaMelody storage
RUN mkdir -p /tmp/javamelody/member-service && chown -R spring:spring /tmp/javamelody

# Copy the built artifact
COPY --from=builder /app/member/target/*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/member/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

