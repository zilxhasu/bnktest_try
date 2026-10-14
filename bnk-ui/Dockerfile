# 多階段建置：Node 編譯 → 只把 dist 放進 nginx，最終映像不含 node_modules
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build

# nginx-unprivileged：官方提供、預設非 root 執行
FROM nginxinc/nginx-unprivileged:1.27-alpine
ENV GATEWAY_URL=http://bnk-gateway:8080
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/browser /usr/share/nginx/html
EXPOSE 8080
