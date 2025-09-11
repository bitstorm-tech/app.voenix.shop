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

# Replace default nginx config with our SPA + API proxy config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

