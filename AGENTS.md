# AGENTS.md

## Scope

This workspace root contains the actual application repo in `Zahaawii/`.

## Verified Workflows

### Local dev stack

From `Zahaawii/infra/compose/README.dev.md`:

```bash
cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii/infra/compose
cp .env.example .env.dev
docker compose -f docker-compose.dev.yml --env-file .env.dev up --build
docker compose -f docker-compose.dev.yml --env-file .env.dev down
docker compose -f docker-compose.dev.yml --env-file .env.dev down -v
```

Useful local endpoints documented there:

- `http://127.0.0.1:8088` for the edge/frontend
- `http://127.0.0.1:8080/api/status/healthz` for the blog backend
- `http://127.0.0.1:8282/healthz` for the MCP server
- `http://127.0.0.1:8181/healthz` for the PersonalChatbotJS service

### CI and build commands

From `Zahaawii/.github/workflows/release.yml`:

```bash
cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii/apps/ZahaawiisBlog
./mvnw -B test

cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii/apps/MCPDatabaseServer
./mvnw -B -DskipTests package

cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii/apps/personalChatBotJS
npm test
```

### Deployment flow

From `Zahaawii/infra/scripts/deploy.sh`:

```bash
cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii
bash infra/scripts/deploy.sh staging
bash infra/scripts/deploy.sh prod
```

Notes:

- The script requires an environment argument: `prod` or `staging`.
- It expects deployed compose and env files to exist under `/opt/${APP_NAME}/{prod|staging}/`.
- If `GHCR_TOKEN` is set, `GHCR_USERNAME` must also be set before deploy.

### Service-local compose files

Verified service-local compose files exist:

- `Zahaawii/apps/ZahaawiisBlog/compose.yaml`
- `Zahaawii/apps/MCPDatabaseServer/compose.yaml`
- `Zahaawii/apps/personalChatBotJS/compose.yaml`

The chatbot README also documents:

```bash
cd /Users/zahaawii/IdeaProjects/ContextEngine_Agents/Zahaawii/apps/personalChatBotJS
docker compose up --build
```

## TODO

- Confirm whether `Zahaawii/apps/MCPDatabaseServer` should run tests in CI; the current workflow packages it with `-DskipTests`.
- Confirm whether a root-level AGENTS convention is expected inside `Zahaawii/` as well; none exists today.
