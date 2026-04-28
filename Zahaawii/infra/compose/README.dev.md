# Local Development Stack

This stack builds and runs services locally (no GHCR pull).

## 1) Prepare env

```bash
cd infra/compose
cp .env.example .env.dev
# then edit .env.dev and set real API keys/passwords
```

## 2) Start stack

```bash
docker compose -f docker-compose.dev.yml --env-file .env.dev up --build
```

## 3) Useful local URLs

- Local edge + blog frontend: `http://127.0.0.1:8088`
- Blog API via edge: `http://127.0.0.1:8088/api/...`
- Node backend via edge: `http://127.0.0.1:8088/node/...`
- MCP via edge: `http://127.0.0.1:8088/mcp/...`
- Blog backend direct health: `http://127.0.0.1:8080/api/status/healthz`
- MCP server direct health: `http://127.0.0.1:8282/healthz`
- PersonalChatbotJS direct health: `http://127.0.0.1:8181/healthz`

## 4) Stop stack

```bash
docker compose -f docker-compose.dev.yml --env-file .env.dev down
```

To also remove local DB and upload volumes:

```bash
docker compose -f docker-compose.dev.yml --env-file .env.dev down -v
```
