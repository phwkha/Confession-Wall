# Stage 1: Build static bundle with Node.js
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy source code and compile production assets
COPY . .
RUN npm run build

# Stage 2: Production runtime with unprivileged Nginx (runs as non-root user)
FROM nginxinc/nginx-unprivileged:alpine

# Copy built assets from builder stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration with SPA routing and API reverse proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Explicitly switch to non-root user for security (SonarQube docker:S6471)
USER nginx

# Expose unprivileged HTTP port 8080
EXPOSE 8080

# Run Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
