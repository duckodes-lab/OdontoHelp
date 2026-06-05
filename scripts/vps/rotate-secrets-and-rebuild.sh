#!/usr/bin/env bash
# Rotação de senha do Postgres + rebuild do stack de produção.
# PRÉ-REQUISITO: editar .env com POSTGRES_PASSWORD, JWT_SECRET e APP_ADMIN_PASSWORD novos.
set -euo pipefail

PROJECT_DIR="${PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
cd "$PROJECT_DIR"

COMPOSE="docker compose -f docker-compose.yml -f docker-compose.prod.yml"
ENV_FILE="${PROJECT_DIR}/.env"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-odontohelp-postgres}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERRO: .env não encontrado em $PROJECT_DIR"
  exit 1
fi

read_env_var() {
  local key="$1"
  grep -E "^${key}=" "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' | sed 's/^["'\'']//;s/["'\'']$//'
}

POSTGRES_USER="$(read_env_var POSTGRES_USER)"
POSTGRES_PASSWORD="$(read_env_var POSTGRES_PASSWORD)"
POSTGRES_DB="$(read_env_var POSTGRES_DB)"
POSTGRES_USER="${POSTGRES_USER:-odontohelp}"
POSTGRES_DB="${POSTGRES_DB:-odontohelp}"

if [[ -z "$POSTGRES_PASSWORD" ]]; then
  echo "ERRO: POSTGRES_PASSWORD não definido no .env"
  exit 1
fi

echo "=== 1. Atualizar senha do Postgres no container (se estiver rodando) ==="
if docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
  escaped_pw="${POSTGRES_PASSWORD//\'/\'\'}"
  docker exec -i "$POSTGRES_CONTAINER" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    -c "ALTER USER ${POSTGRES_USER} WITH PASSWORD '${escaped_pw}';"
  echo "Senha do Postgres atualizada no banco."
else
  echo "Container $POSTGRES_CONTAINER não está rodando — senha será usada no próximo up."
fi

echo ""
echo "=== 2. Rebuild e subida do stack ==="
$COMPOSE up -d --build

echo ""
echo "=== 3. Aguardando backend (30s) ==="
sleep 10
for i in {1..6}; do
  if docker logs odontohelp-backend --tail 5 2>&1 | grep -q "Started"; then
    echo "Backend iniciado."
    break
  fi
  echo "Aguardando... ($i/6)"
  sleep 5
done

echo ""
echo "=== 4. Últimos logs do backend ==="
docker logs odontohelp-backend --tail 40 2>&1 || true

echo ""
echo "=== Concluído ==="
echo "- JWT antigo invalidado (se JWT_SECRET foi trocado no .env)"
echo "- Faça logout/login em todos os clientes"
echo "- Desative ou troque senha do usuário de teste do pentest"
