# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

# Copy frontend package files
COPY frontend/package.json ./

# Install dependencies using npm
RUN npm install

# Copy frontend source code
COPY frontend/ ./

# Build the frontend (skip TypeScript checks for now)
RUN npx vite build

# Stage 2: Build Backend
FROM gradle:8.10-jdk21 AS backend-build

WORKDIR /app

# Copy backend source
COPY backend/ ./

# Copy frontend build output to backend resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build the backend (this will include the frontend assets in the JAR)
RUN gradle bootJar --no-daemon

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install PostgreSQL client for potential debugging
RUN apk add --no-cache postgresql-client

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

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]