FROM node:22 AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY tsconfig.json ./
COPY src ./src
RUN npm run build && npm prune --omit=dev

FROM node:22-slim

WORKDIR /app
COPY --from=build /build/package.json ./
COPY --from=build /build/node_modules ./node_modules
COPY --from=build /build/dist ./dist

RUN mkdir -p /data && chown node:node /data
ENV NODE_ENV=production \
    PORT=3333 \
    AUTH_STATE_DIR=/data/auth_sessions \
    FILA_DIR=/data/fila_webhook \
    ARQUIVO_IDEMPOTENCIA=/data/envios-idempotencia.json

USER node
EXPOSE 3333

CMD ["node", "dist/index.js"]
