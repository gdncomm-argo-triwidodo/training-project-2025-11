# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy generic-framework first (shared dependency)
COPY generic-framework /app/generic-framework

# Copy Maven wrapper and pom.xml for dependency caching
COPY cart/pom.xml /app/cart/
COPY cart/.mvn /app/cart/.mvn/ 2>/dev/null || true
COPY cart/mvnw /app/cart/ 2>/dev/null || true

WORKDIR /app/cart

# Install Maven if not using wrapper
RUN apk add --no-cache maven

# Install generic-framework dependency
RUN cd /app/generic-framework && mvn clean install -DskipTests -q

# Copy source code
COPY cart/src /app/cart/src

# Build the application
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directory for JavaMelody storage
RUN mkdir -p /tmp/javamelody/cart-service && chown -R spring:spring /tmp/javamelody

# Copy the built artifact
COPY --from=builder /app/cart/target/*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

