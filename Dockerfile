# Stage 1: Build Frontend
# This stage builds the React frontend. A multi-stage build is used to keep the final image lean.
FROM node:24.3-alpine AS frontend-build

WORKDIR /app/frontend

# Copy package.json and package-lock.json first to leverage Docker's layer caching.
# This layer is only rebuilt when dependencies in package.json change.
COPY frontend/package.json frontend/package-lock.json ./

# Install frontend dependencies using npm ci for clean, reproducible builds.
RUN npm ci

# Copy the rest of the frontend source code.
# The .dockerignore file ensures that local node_modules and other unnecessary files are not copied.
COPY frontend/ ./

# Build the frontend for production. The output will be in the /app/frontend/dist directory.
RUN npm run build

# Stage 2: Build Backend
# This stage builds the Spring Boot backend application.
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

# Copy Gradle wrapper and build configuration files.
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
COPY backend/gradlew ./
COPY backend/gradle ./gradle

# Make the Gradle wrapper executable.
RUN chmod +x ./gradlew

# Download dependencies as a separate layer to leverage Docker cache.
# This avoids re-downloading dependencies on every source code change.
# Using './gradlew dependencies' is a safe way to pre-fetch dependencies.
RUN ./gradlew dependencies --no-daemon

# Copy the backend source code.
COPY backend/src ./src

# Copy the built frontend assets from the frontend-build stage into the backend's static resources.
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build the application JAR. The --no-daemon flag is recommended for CI environments.
RUN ./gradlew bootJar --no-daemon

# Stage 3: Runtime
# This is the final, production-ready image. It uses a smaller JRE for a reduced footprint.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a dedicated non-root user and group for enhanced security.
RUN apk add --no-cache ca-certificates magic-wormhole-rs \
    && addgroup -g 1000 spring \
    && adduser -u 1000 -G spring -s /bin/sh -D spring

# Copy the application JAR from the build stage.
# Using a wildcard is less safe. For a more robust build, replace with the exact JAR name.
# Example: COPY --from=backend-build /app/build/libs/voenix-0.0.1.jar app.jar
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Change ownership of the application directory to the non-root user.
RUN chown -R spring:spring /app

# Switch to the non-root user.
USER spring

# Expose the port the application will run on.
EXPOSE 8080

# Set the default Spring profile to 'prod'. This can be overridden at runtime.
ENV SPRING_PROFILES_ACTIVE=prod

# Set default JVM options. These can be overridden at runtime by setting the JAVA_OPTS environment variable.
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# The command to run the application.
# Using "sh -c" allows the JAVA_OPTS environment variable to be expanded.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]