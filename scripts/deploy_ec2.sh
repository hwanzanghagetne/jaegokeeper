#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# ---- Config (override with env vars) ----
EC2_USER="${EC2_USER:-ubuntu}"
EC2_HOST="${EC2_HOST:-98.84.8.224}"
EC2_KEY="${EC2_KEY:-$HOME/.ssh/jaegokeeper-key-20260407.pem}"

TOMCAT_HOME="${TOMCAT_HOME:-/home/ubuntu/apache-tomcat-9.0.98}"
REMOTE_STAGING_WAR="${REMOTE_STAGING_WAR:-/home/ubuntu/JaegoKeeper.war.upload}"
HEALTHCHECK_URL="${HEALTHCHECK_URL:-http://127.0.0.1/}"

WAR_NAME="${WAR_NAME:-JaegoKeeper.war}"
LOCAL_WAR_PATH="${LOCAL_WAR_PATH:-$ROOT_DIR/target/$WAR_NAME}"
MVN_CMD="${MVN_CMD:-}"
SKIP_TESTS="${SKIP_TESTS:-true}"

log() {
  printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"
}

require_file() {
  local p="$1"
  if [[ ! -f "$p" ]]; then
    echo "Required file not found: $p" >&2
    exit 1
  fi
}

resolve_mvn_cmd() {
  if [[ -n "$MVN_CMD" ]]; then
    echo "$MVN_CMD"
    return 0
  fi
  if command -v mvn >/dev/null 2>&1; then
    echo "mvn"
    return 0
  fi
  if [[ -x "$ROOT_DIR/tools/apache-maven-3.9.9/bin/mvn" ]]; then
    echo "$ROOT_DIR/tools/apache-maven-3.9.9/bin/mvn"
    return 0
  fi
  echo "mvn command not found. Set MVN_CMD or install Maven." >&2
  exit 1
}

build_war() {
  local mvn
  mvn="$(resolve_mvn_cmd)"
  log "Building WAR with: $mvn"
  if [[ "$SKIP_TESTS" == "true" ]]; then
    "$mvn" -q clean package -DskipTests
  else
    "$mvn" -q clean package
  fi
  require_file "$LOCAL_WAR_PATH"
  log "WAR built: $LOCAL_WAR_PATH"
}

upload_war() {
  require_file "$EC2_KEY"
  require_file "$LOCAL_WAR_PATH"
  log "Uploading WAR -> ${EC2_USER}@${EC2_HOST}:${REMOTE_STAGING_WAR}"
  scp -i "$EC2_KEY" "$LOCAL_WAR_PATH" "${EC2_USER}@${EC2_HOST}:${REMOTE_STAGING_WAR}"
}

deploy_remote() {
  require_file "$EC2_KEY"
  log "Deploying on EC2 (Tomcat restart + health check)"

  ssh -i "$EC2_KEY" "${EC2_USER}@${EC2_HOST}" \
    "REMOTE_STAGING_WAR='$REMOTE_STAGING_WAR' TOMCAT_HOME='$TOMCAT_HOME' HEALTHCHECK_URL='$HEALTHCHECK_URL' bash -s" <<'EOF'
set -euo pipefail

ROOT_WAR="$TOMCAT_HOME/webapps/ROOT.war"
ROOT_DIR="$TOMCAT_HOME/webapps/ROOT"
BACKUP_WAR="$TOMCAT_HOME/webapps/ROOT.war.bak.$(date +%Y%m%d_%H%M%S)"

if [[ ! -f "$REMOTE_STAGING_WAR" ]]; then
  echo "Uploaded WAR not found: $REMOTE_STAGING_WAR" >&2
  exit 1
fi

if [[ -f "$ROOT_WAR" ]]; then
  cp "$ROOT_WAR" "$BACKUP_WAR"
fi

"$TOMCAT_HOME/bin/shutdown.sh" || true
sleep 3

rm -rf "$ROOT_DIR"
cp "$REMOTE_STAGING_WAR" "$ROOT_WAR"

"$TOMCAT_HOME/bin/startup.sh"

for i in $(seq 1 40); do
  if curl -fsS "$HEALTHCHECK_URL" >/dev/null 2>&1; then
    echo "Health check OK: $HEALTHCHECK_URL"
    rm -f "$REMOTE_STAGING_WAR"
    exit 0
  fi
  sleep 2
done

echo "Health check failed: $HEALTHCHECK_URL" >&2
exit 1
EOF
}

main() {
  log "Deploy start"
  build_war
  upload_war
  deploy_remote
  log "Deploy done"
}

main "$@"

