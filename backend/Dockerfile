# Multi-stage Dockerfile for Dynamic QR Ticketing Backend
# Optimized for Google Cloud Run (Free Tier) & Alpine Linux (~180MB)

# Stage 1: Build JAR using Gradle & JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle wrapper and build configuration
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Pre-download dependencies layer for faster cached builds
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code and build executable Spring Boot JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root system user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Cloud Run dynamic PORT variable (default 8080)
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Configure container-aware JVM flags
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar app.jar"]
