# syntax=docker/dockerfile:1

############################
# Build stage
############################
FROM node:20-alpine AS builder
WORKDIR /app

# Install dependencies (use lockfile for reproducible builds)
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

############################
# Runtime stage
############################
FROM nginx:1.27-alpine AS runner

# Install envsubst for runtime templating
RUN apk add --no-cache gettext

# Default API origin; override at runtime with -e API_ORIGIN=...
ENV API_ORIGIN=http://app:8080

# Copy nginx config template (rendered at container start)
COPY nginx.conf /etc/nginx/conf.d/default.conf.template

# Copy compiled assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

# Render template with env vars, then start nginx
CMD ["sh", "-c", "envsubst '$API_ORIGIN' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
