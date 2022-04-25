FROM node:16.13.2

WORKDIR /app

COPY frontend/package.json ./
COPY frontend/package-lock.json ./

RUN npm ci --silent && \
    npm install react-scripts@3.4.1 -g --silent

COPY frontend/ ./

ENV PATH=/app/node_modules/bin:$PATH

ENTRYPOINT ["npm", "start"]