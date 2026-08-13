#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <git-sha> <docker-image>" >&2
  exit 2
fi

GIT_SHA="$1"
NEW_IMAGE="$2"

if [[ ! "${GIT_SHA}" =~ ^[0-9a-f]{40}$ ]]; then
  echo "Invalid Git SHA: ${GIT_SHA}" >&2
  exit 2
fi

APP_DIR="/opt/jaegokeeper"
COMPOSE_FILE="${APP_DIR}/compose.prod.yaml"
COMPOSE_URL="https://raw.githubusercontent.com/hwanzanghagetne/jaegokeeper/${GIT_SHA}/compose.prod.yaml"
HEALTHCHECK_URL="http://127.0.0.1:8080/"
TEMP_COMPOSE="$(mktemp)"

cleanup() {
  rm -f "${TEMP_COMPOSE}"
}
trap cleanup EXIT

test -f "${APP_DIR}/.env"
curl -fsSL "${COMPOSE_URL}" -o "${TEMP_COMPOSE}"
BACKEND_IMAGE="${NEW_IMAGE}" BACKEND_PORT=8080 \
  docker compose -f "${TEMP_COMPOSE}" config --quiet
install -m 600 "${TEMP_COMPOSE}" "${COMPOSE_FILE}"

PREVIOUS_IMAGE="$(docker inspect --format '{{.Config.Image}}' jaegokeeper-backend 2>/dev/null || true)"

cd "${APP_DIR}"
export BACKEND_IMAGE="${NEW_IMAGE}"
export BACKEND_PORT=8080

docker compose -f "${COMPOSE_FILE}" pull

if docker compose -f "${COMPOSE_FILE}" up -d --force-recreate --wait --wait-timeout 90 \
  && curl -fsS "${HEALTHCHECK_URL}" > /dev/null; then
  echo "Deploy succeeded: ${NEW_IMAGE}"
  docker image prune -f > /dev/null
  exit 0
fi

echo "Deploy failed. Starting rollback..." >&2

if [[ -n "${PREVIOUS_IMAGE}" ]]; then
  export BACKEND_IMAGE="${PREVIOUS_IMAGE}"
  if docker compose -f "${COMPOSE_FILE}" up -d --force-recreate --wait --wait-timeout 90 \
    && curl -fsS "${HEALTHCHECK_URL}" > /dev/null; then
    echo "Docker rollback succeeded: ${PREVIOUS_IMAGE}" >&2
    exit 1
  fi
fi

echo "Docker rollback failed" >&2
exit 1
