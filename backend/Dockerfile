# Multi-stage build: compile TypeScript, then run with only production deps.
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production

# Run as a non-root user - if the process is ever compromised, it has
# no permission to modify app files or install anything system-wide.
RUN addgroup -S lingua && adduser -S lingua -G lingua
COPY package*.json ./
RUN npm ci --omit=dev
COPY --from=builder /app/dist ./dist
USER lingua

EXPOSE 3000
CMD ["node", "dist/server.js"]
