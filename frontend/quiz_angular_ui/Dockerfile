# =============================================================================
# frontend/Dockerfile
# Multi-stage: Stage 1 = Node build, Stage 2 = Nginx static serve
# =============================================================================

# ── Stage 1: Build Angular ────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first (layer cache)
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# Copy source and build
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Nginx ───────────────────────────────────────────────────────────
FROM nginx:1.27-alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built Angular files from builder stage
# Angular 17+ outputs to dist/<project-name>/browser
COPY --from=builder /app/dist/angular-frontend-quiz_application/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Nginx runs as non-root by default in the official image
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
