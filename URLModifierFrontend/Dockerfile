# ── 1단계: Vue 빌드 ──────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# PUBLIC_URL을 빌드 인수로 받아 VITE_API_URL에 주입
# 예) http://mysite.com → FE가 같은 도메인 Nginx를 통해 BE 프록시
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}

RUN npm run build

# 빌드 결과물(/app/dist)은 docker-compose의 fe_static 볼륨으로 복사됩니다.
# Nginx 서빙은 루트의 standalone nginx 컨테이너가 담당합니다.
