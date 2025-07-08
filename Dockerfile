# Stage 1: Build Frontend
FROM oven/bun:1.0-alpine AS frontend-build

WORKDIR /app/frontend

# Copy frontend package and lock files
COPY frontend/package.json frontend/bun.lock ./

# Install dependencies using bun
RUN bun install --frozen-lockfile

# Copy frontend source code
COPY frontend/ ./

# Build the frontend
RUN bun run build

# Stage 2: Build Backend
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

# Copy dependency-related files first
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
COPY backend/gradlew ./
COPY backend/gradle ./gradle

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer will be cached)
RUN ./gradlew build --no-daemon -x test || true

# Copy the rest of the source code
COPY backend/src ./src

# Copy frontend build output to backend resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build the backend using the wrapper
RUN ./gradlew bootJar --no-daemon

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Copy the JAR from build stage
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 8080

# Set Spring profile to production
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application with explicit JVM options
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app/app.jar"]