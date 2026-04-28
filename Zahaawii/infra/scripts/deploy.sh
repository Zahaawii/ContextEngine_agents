#!/usr/bin/env bash
set -Eeuo pipefail

usage() {
  echo "Usage: $0 <prod|staging>"
  exit 1
}

[[ $# -eq 1 ]] || usage
ENVIRONMENT="$1"

if [[ "$ENVIRONMENT" != "prod" && "$ENVIRONMENT" != "staging" ]]; then
  usage
fi

APP_NAME="${APP_NAME:-<APP_NAME>}"
BASE_DIR="${BASE_DIR:-/opt/${APP_NAME}}"
ENV_DIR="${BASE_DIR}/${ENVIRONMENT}"
COMPOSE_FILE="${COMPOSE_FILE:-${ENV_DIR}/docker-compose.yml}"
ENV_FILE="${ENV_FILE:-${ENV_DIR}/.env}"
DEFAULT_PROJECT_NAME="$(printf '%s-%s' "${APP_NAME}" "${ENVIRONMENT}" | tr '[:upper:]' '[:lower:]')"
PROJECT_NAME="${PROJECT_NAME:-${DEFAULT_PROJECT_NAME}}"

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Compose file not found: $COMPOSE_FILE"
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Env file not found: $ENV_FILE"
  exit 1
fi

if [[ -n "${GHCR_TOKEN:-}" ]]; then
  : "${GHCR_USERNAME:?GHCR_USERNAME is required when GHCR_TOKEN is set}"
  printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

docker compose --project-name "$PROJECT_NAME" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull
docker compose --project-name "$PROJECT_NAME" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --remove-orphans

if [[ "${PRUNE_IMAGES:-false}" == "true" ]]; then
  docker image prune -f
fi

echo "Deployment completed for ${PROJECT_NAME}"
