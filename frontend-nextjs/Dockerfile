# 1. Install dependencies
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install

# 2. Build the application
FROM node:22-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# Make sure your build script is configured in package.json
RUN npm run build

# 3. Production image
FROM node:22-alpine AS runner
WORKDIR /app

# Set production environment
ENV NODE_ENV=production

# Copy the standalone output from the builder stage
COPY --from=builder /app/.next/standalone ./
# Copy the static assets
COPY --from=builder /app/.next/static ./.next/static
# Copy public assets
COPY --from=builder /app/public ./public

EXPOSE 3000
ENV PORT 3000

# Run the server
CMD ["node", "server.js"]