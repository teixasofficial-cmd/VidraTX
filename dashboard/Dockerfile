FROM node:22-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ENV VITE_API_BASE_URL=
RUN npm run build

FROM caddy:2-alpine
COPY --from=build /build/dist /srv
