# syntax=docker/dockerfile:1.7

FROM node:22-alpine AS deps
WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

FROM node:22-alpine AS runtime
ENV NODE_ENV=production
ENV PORT=8181

WORKDIR /app
RUN addgroup -S app && adduser -S -G app app

COPY --from=deps /app/node_modules ./node_modules
COPY Backend ./Backend
COPY Frontend ./llm
COPY index.html ./index.html

USER app
EXPOSE 8181

CMD ["node", "Backend/geminiWithMCP.js"]
